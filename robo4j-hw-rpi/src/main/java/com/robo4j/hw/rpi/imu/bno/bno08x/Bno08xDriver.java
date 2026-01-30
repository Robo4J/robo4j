/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.rpi.imu.bno.bno08x;

import com.robo4j.hw.rpi.imu.bno.ActivityClassifierEvent;
import com.robo4j.hw.rpi.imu.bno.Bno080Device;
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.hw.rpi.imu.bno.DataEventType;
import com.robo4j.hw.rpi.imu.bno.DataListener;
import com.robo4j.hw.rpi.imu.bno.GyroIntegratedRVEvent;
import com.robo4j.hw.rpi.imu.bno.StabilityClassifierEvent;
import com.robo4j.hw.rpi.imu.bno.StepCounterEvent;
import com.robo4j.hw.rpi.imu.bno.TapDetectorEvent;
import com.robo4j.hw.rpi.imu.bno.VectorEvent;
import com.robo4j.hw.rpi.imu.bno.shtp.ControlReportId;
import com.robo4j.hw.rpi.imu.bno.shtp.SensorReportId;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpChannel;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpCommandId;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpOperation;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpOperationBuilder;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpOperationResponse;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketResponse;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpReportIds;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils;
import com.robo4j.math.geometry.Tuple3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.EMPTY_EVENT;
import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.intToFloat;

/**
 * Unified BNO08x driver that delegates physical communication to an
 * {@link ShtpTransport}. Supports both SPI and I2C transports via composition.
 *
 * <p>
 * Channel 0: command channel<br>
 * Channel 1: executable<br>
 * Channel 2: sensor hub control channel<br>
 * Channel 3: input sensor reports (non-wake, not gyroRV)<br>
 * Channel 4: wake input sensor reports<br>
 * Channel 5: gyro rotation vector<br>
 * </p>
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno08xDriver implements Bno080Device {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bno08xDriver.class);

    static final byte RECEIVE_WRITE_BYTE = (byte) 0xFF;
    static final byte RECEIVE_WRITE_BYTE_CONTINUAL = (byte) 0;

    private static final int TIMEOUT_SEC = 1;
    private static final int MAX_COUNTER = 255;
    private static final int MAX_SPI_WAIT_CYCLES = 2;
    private static final short CHANNEL_COUNT = 6;
    private static final int AWAIT_TERMINATION = 10;

    // Executable channel commands
    private static final int EXECUTABLE_CMD_RESET = 1;
    private static final int EXECUTABLE_CMD_ON = 2;
    private static final int EXECUTABLE_CMD_SLEEP = 3;

    // Tare subcommands
    private static final int TARE_NOW = 0;
    private static final int TARE_PERSIST = 1;
    private static final int TARE_SET_REORIENTATION = 2;

    // Calibration config flags
    private static final int CAL_ACCEL = 0x01;
    private static final int CAL_GYRO = 0x02;
    private static final int CAL_MAG = 0x04;

    private final ShtpTransport transport;
    private final List<DataListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final AtomicBoolean resetOccurred = new AtomicBoolean(false);
    private final AtomicInteger commandSequenceNumber = new AtomicInteger(0);
    private final AtomicInteger waitCounter = new AtomicInteger(0);
    private final int[] sequenceNumberByChannel = new int[CHANNEL_COUNT];
    private volatile int lastResetReason = 0;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, (r) -> {
        Thread t = new Thread(r, "Bno08x Internal Executor");
        t.setDaemon(true);
        return t;
    });

    /**
     * Creates a new BNO08x driver with the given transport.
     *
     * @param transport the SHTP transport to use for communication
     */
    public Bno08xDriver(ShtpTransport transport) {
        this.transport = transport;
    }

    @Override
    public void addListener(DataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean start(SensorReportId report, int reportPeriod) {
        if (reportPeriod > 0) {
            final CountDownLatch latch = new CountDownLatch(1);
            LOGGER.info("START: ready:{}, active:{}", ready.get(), active.get());
            waitCounter.set(0);
            synchronized (executor) {
                if (!ready.get()) {
                    initAndActive(latch, report, reportPeriod);
                } else {
                    reactivate(latch, report, reportPeriod);
                }
                if (waitForLatch(latch)) {
                    executor.execute(() -> {
                        active.set(ready.get());
                        executeListenerJob();
                    });
                }
            }
        } else {
            LOGGER.info("start: not valid sensor:{} delay:{}", report, reportPeriod);
        }
        return active.get();
    }

    @Override
    public boolean stop() {
        if (ready.get() && active.get()) {
            active.set(false);
            if (softReset()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(700);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("not possible stop");
                }
                return true;
            } else {
                LOGGER.debug("SOFT FALSE");
            }
        }
        return false;
    }

    @Override
    public void shutdown() {
        synchronized (executor) {
            active.set(false);
            ready.set(false);
            awaitTermination();
            transport.close();
        }
    }

    @Override
    public void calibrate(long timeout) {
        ShtpPacketRequest calibrateCommand = createCalibrateCommandAll();
        final CountDownLatch latch = new CountDownLatch(1);
        executor.submit(() -> {
            try {
                transport.sendPacket(calibrateCommand);
            } catch (InterruptedException | IOException e) {
                LOGGER.error("Calibration failed! e:{}", e.getMessage(), e);
            }
            latch.countDown();
        });
    }

    public boolean isActive() {
        return active.get();
    }

    // --- Tare operations ---

    @Override
    public boolean tareNow(boolean zAxisOnly, TareBasis basis) {
        int axes = zAxisOnly ? TareAxis.Z : TareAxis.ALL;
        ShtpPacketRequest packet = createTareCommand(TARE_NOW, axes, basis.getId());
        return sendCommandPacket(packet);
    }

    @Override
    public boolean saveTare() {
        ShtpPacketRequest packet = createTareCommand(TARE_PERSIST, 0, 0);
        return sendCommandPacket(packet);
    }

    @Override
    public boolean clearTare() {
        ShtpPacketRequest packet = createTareCommand(TARE_SET_REORIENTATION, 0, 0);
        return sendCommandPacket(packet);
    }

    private ShtpPacketRequest createTareCommand(int subcommand, int axes, int basis) {
        ShtpPacketRequest packet = prepareShtpPacketRequest(ShtpChannel.CONTROL, 12);
        packet.addBody(0, ControlReportId.COMMAND_REQUEST.getId());
        packet.addBody(1, commandSequenceNumber.getAndIncrement());
        packet.addBody(2, ShtpCommandId.TARE.getId());
        packet.addBody(3, subcommand);
        packet.addBody(4, axes);
        packet.addBody(5, basis);
        return packet;
    }

    // --- Dynamic Calibration Data (DCD) operations ---

    @Override
    public boolean saveCalibration() {
        ShtpPacketRequest packet = prepareShtpPacketRequest(ShtpChannel.CONTROL, 12);
        packet.addBody(0, ControlReportId.COMMAND_REQUEST.getId());
        packet.addBody(1, commandSequenceNumber.getAndIncrement());
        packet.addBody(2, ShtpCommandId.DCD.getId());
        return sendCommandPacket(packet);
    }

    @Override
    public boolean setCalibrationConfig(boolean accel, boolean gyro, boolean mag) {
        int sensors = 0;
        if (accel) sensors |= CAL_ACCEL;
        if (gyro) sensors |= CAL_GYRO;
        if (mag) sensors |= CAL_MAG;

        ShtpPacketRequest packet = prepareShtpPacketRequest(ShtpChannel.CONTROL, 12);
        packet.addBody(0, ControlReportId.COMMAND_REQUEST.getId());
        packet.addBody(1, commandSequenceNumber.getAndIncrement());
        packet.addBody(2, ShtpCommandId.ME_CALIBRATE.getId());
        packet.addBody(3, (accel ? 1 : 0));
        packet.addBody(4, (gyro ? 1 : 0));
        packet.addBody(5, (mag ? 1 : 0));
        return sendCommandPacket(packet);
    }

    // --- Power management ---

    @Override
    public boolean sleep() {
        return sendExecutableCommand(EXECUTABLE_CMD_SLEEP);
    }

    @Override
    public boolean wake() {
        return sendExecutableCommand(EXECUTABLE_CMD_ON);
    }

    private boolean sendExecutableCommand(int command) {
        ShtpPacketRequest packet = prepareShtpPacketRequest(ShtpChannel.EXECUTABLE, 1);
        packet.addBody(0, command);
        return sendCommandPacket(packet);
    }

    // --- Status ---

    @Override
    public boolean wasReset() {
        return resetOccurred.getAndSet(false);
    }

    @Override
    public int getResetReason() {
        return lastResetReason;
    }

    /**
     * Internal method to mark that a reset occurred.
     * Called when we detect a reset complete message from the device.
     */
    void markResetOccurred(int reason) {
        lastResetReason = reason;
        resetOccurred.set(true);
    }

    private boolean sendCommandPacket(ShtpPacketRequest packet) {
        try {
            return transport.sendPacket(packet);
        } catch (InterruptedException | IOException e) {
            LOGGER.error("Failed to send command packet: {}", e.getMessage(), e);
            return false;
        }
    }

    // --- Protocol-level methods ---

    ShtpPacketRequest prepareShtpPacketRequest(ShtpChannel shtpChannel, int size) {
        ShtpPacketRequest packet = new ShtpPacketRequest(size, sequenceNumberByChannel[shtpChannel.getChannel()]++);
        packet.createHeader(shtpChannel);
        return packet;
    }

    ShtpPacketRequest getProductIdRequest() {
        ShtpPacketRequest result = prepareShtpPacketRequest(ShtpChannel.CONTROL, 2);
        result.addBody(0, ControlReportId.PRODUCT_ID_REQUEST.getId());
        result.addBody(1, 0);
        return result;
    }

    ShtpPacketRequest getSoftResetPacket() {
        ShtpPacketRequest packet = prepareShtpPacketRequest(ShtpChannel.EXECUTABLE, 1);
        packet.addBody(0, 1);
        return packet;
    }

    /**
     * Calibration command. Bug fix: position 1 writes the command sequence number
     * (was incorrectly writing to position 0 twice in the old implementation).
     */
    private ShtpPacketRequest createCalibrateCommandAll() {
        ShtpChannel shtpChannel = ShtpChannel.COMMAND;
        ShtpPacketRequest packet = prepareShtpPacketRequest(shtpChannel, 12);
        packet.addBody(0, ControlReportId.COMMAND_REQUEST.getId());
        packet.addBody(1, commandSequenceNumber.getAndIncrement());
        packet.addBody(2, ShtpCommandId.ME_CALIBRATE.getId());
        packet.addBody(3, 1);
        packet.addBody(4, 1);
        packet.addBody(5, 1);
        return packet;
    }

    private ShtpOperation getSensorReportOperation(SensorReportId report, int reportDelay) {
        ShtpOperationResponse response = new ShtpOperationResponse(ControlReportId.GET_FEATURE_RESPONSE);
        ShtpPacketRequest request = createFeatureRequest(report, reportDelay, 0);
        return new ShtpOperation(request, response);
    }

    private boolean enableSensorReport(SensorReportId report, int reportDelay) {
        final ShtpOperation enableSensorReportOp = getSensorReportOperation(report, reportDelay);
        try {
            return processOperationChainByHead(enableSensorReportOp);
        } catch (InterruptedException | IOException e) {
            LOGGER.error("ERROR enableSensorReport:{}", e.getMessage(), e);
            return false;
        }
    }

    private boolean processOperationChainByHead(ShtpOperation head) throws InterruptedException, IOException {
        int counter = 0;
        boolean state = true;
        do {
            if (head.hasRequest()) {
                transport.sendPacket(head.getRequest());
            }
            boolean waitForResponse = true;
            while (waitForResponse && counter < MAX_COUNTER) {
                ShtpPacketResponse response = transport.receivePacket(false, RECEIVE_WRITE_BYTE);
                ShtpOperationResponse opResponse = new ShtpOperationResponse(
                        ShtpChannel.getByChannel(response.getHeaderChannel()), response.getBodyFirst());
                if (head.getResponse().equals(opResponse)) {
                    waitForResponse = false;
                } else {
                    transport.waitForDevice();
                }
                counter++;
            }
            head = head.getNext();
            if (state && counter >= MAX_COUNTER) {
                state = false;
            }
        } while (head != null);
        return state;
    }

    private DataEvent3f processReceivedPacket() {
        try {
            transport.waitForDevice();
            ShtpPacketResponse receivedPacket = transport.receivePacket(true, RECEIVE_WRITE_BYTE_CONTINUAL);
            ShtpChannel channel = ShtpChannel.getByChannel(receivedPacket.getHeaderChannel());
            ShtpReportIds reportType = getReportType(channel, receivedPacket);

            switch (channel) {
                case CONTROL:
                    break;
                case REPORTS:
                    if (SensorReportId.BASE_TIMESTAMP.equals(reportType)) {
                        return parseInputReport(receivedPacket);
                    }
                    break;
                default:
                    break;
            }
            LOGGER.debug("not implemented channel:{}, report:{}", channel, reportType);
            return EMPTY_EVENT;

        } catch (IOException | InterruptedException e) {
            LOGGER.error("ERROR: processReceivedPacket e:{}", e.getMessage(), e);
            return EMPTY_EVENT;
        }
    }

    private ShtpReportIds getReportType(ShtpChannel channel, ShtpPacketResponse response) {
        return switch (channel) {
            case CONTROL -> ControlReportId.getById(response.getBodyFirst());
            case REPORTS -> SensorReportId.getById(response.getBodyFirst());
            default -> ControlReportId.NONE;
        };
    }

    private DataEvent3f parseInputReport(ShtpPacketResponse packet) {
        int[] payload = packet.getBody();
        final int dataLength = packet.getBodySize();
        long timeStamp = (payload[4] << 24) | (payload[3] << 16) | (payload[2] << 8) | (payload[1]);

        long accDelay = 17;
        transport.setSensorReportDelay(timeStamp + accDelay);

        int sensor = payload[5];
        int status = (payload[7] & 0x03) & 0xFF;
        int data1 = ((payload[10] << 8) & 0xFFFF) | payload[9] & 0xFF;
        int data2 = (payload[12] << 8 & 0xFFFF | (payload[11]) & 0xFF);
        int data3 = (payload[14] << 8 & 0xFFFF) | (payload[13] & 0xFF);
        int data4 = 0;
        int data5 = 0;

        if (payload.length > 15 && dataLength - 5 > 9) {
            data4 = (payload[16] & 0xFFFF) << 8 | payload[15] & 0xFF;
        }
        if (payload.length > 17 && dataLength - 5 > 11) {
            data5 = (payload[18] & 0xFFFF) << 8 | payload[17] & 0xFF;
        }

        final SensorReportId sensorReport = SensorReportId.getById(sensor);

        return switch (sensorReport) {
            // Basic 3-axis sensors
            case ACCELEROMETER ->
                    createDataEvent(DataEventType.ACCELEROMETER, timeStamp, status, data1, data2, data3, data4);
            case RAW_ACCELEROMETER ->
                    createDataEvent(DataEventType.ACCELEROMETER_RAW, timeStamp, status, data1, data2, data3, data4);
            case LINEAR_ACCELERATION ->
                    createDataEvent(DataEventType.ACCELEROMETER_LINEAR, timeStamp, status, data1, data2, data3, data4);
            case GRAVITY ->
                    createDataEvent(DataEventType.GRAVITY, timeStamp, status, data1, data2, data3, data4);
            case GYROSCOPE ->
                    createDataEvent(DataEventType.GYROSCOPE, timeStamp, status, data1, data2, data3, data4);
            case GYRO_UNCALIBRATED ->
                    createDataEvent(DataEventType.GYROSCOPE_UNCALIBRATED, timeStamp, status, data1, data2, data3, data4);
            case MAGNETIC_FIELD ->
                    createDataEvent(DataEventType.MAGNETOMETER, timeStamp, status, data1, data2, data3, data4);
            case MAGNETIC_FIELD_UNCALIBRATED ->
                    createDataEvent(DataEventType.MAGNETOMETER_UNCALIBRATED, timeStamp, status, data1, data2, data3, data4);

            // Rotation vectors
            case ROTATION_VECTOR ->
                    createVectorEvent(DataEventType.VECTOR_ROTATION, timeStamp, status, data1, data2, data3, data4, data5);
            case GAME_ROTATION_VECTOR ->
                    createVectorEvent(DataEventType.VECTOR_GAME, timeStamp, status, data1, data2, data3, data4, data5);
            case GEOMAGNETIC_ROTATION_VECTOR ->
                    createVectorEvent(DataEventType.VECTOR_GEOMAGNETIC, timeStamp, status, data1, data2, data3, data4, data5);
            case ARVR_STAB_ROTATION_VECTOR ->
                    createVectorEvent(DataEventType.VECTOR_ARVR_STABILIZED, timeStamp, status, data1, data2, data3, data4, data5);
            case ARVR_STAB_GAME_ROTATION_VECTOR ->
                    createVectorEvent(DataEventType.VECTOR_ARVR_GAME_STABILIZED, timeStamp, status, data1, data2, data3, data4, data5);
            case GYRO_INT_ROTATION_VECTOR ->
                    createGyroIntegratedRVEvent(timeStamp, status, payload);

            // Activity/motion sensors
            case STEP_COUNTER ->
                    createStepCounterEvent(timeStamp, status, payload);
            case TAP_DETECTOR ->
                    new TapDetectorEvent(status, timeStamp, payload[5] & 0xFF);
            case STABILITY_CLASSIFIER ->
                    new StabilityClassifierEvent(status, timeStamp, payload[5] & 0xFF);
            case PERSONAL_ACTIVITY_CLASSIFIER ->
                    createActivityClassifierEvent(timeStamp, status, payload);

            default -> EMPTY_EVENT;
        };
    }

    private DataEvent3f createGyroIntegratedRVEvent(long timeStamp, int status, int[] payload) {
        // Gyro integrated RV has quaternion (i,j,k,real) + angular velocity (x,y,z)
        int qI = ((payload[10] << 8) & 0xFFFF) | (payload[9] & 0xFF);
        int qJ = ((payload[12] << 8) & 0xFFFF) | (payload[11] & 0xFF);
        int qK = ((payload[14] << 8) & 0xFFFF) | (payload[13] & 0xFF);
        int qReal = ((payload[16] << 8) & 0xFFFF) | (payload[15] & 0xFF);
        int angVelX = ((payload[18] << 8) & 0xFFFF) | (payload[17] & 0xFF);
        int angVelY = ((payload[20] << 8) & 0xFFFF) | (payload[19] & 0xFF);
        int angVelZ = ((payload[22] << 8) & 0xFFFF) | (payload[21] & 0xFF);

        int qPoint = DataEventType.GYRO_INTEGRATED_RV.getQ();
        int angVelQPoint = 10; // Angular velocity Q point
        Tuple3f quaternionIJK = ShtpUtils.createTupleFromFixed(qPoint, qI, qJ, qK);

        return new GyroIntegratedRVEvent(status, quaternionIJK, timeStamp,
                intToFloat(qReal, qPoint),
                intToFloat(angVelX, angVelQPoint),
                intToFloat(angVelY, angVelQPoint),
                intToFloat(angVelZ, angVelQPoint));
    }

    private DataEvent3f createStepCounterEvent(long timeStamp, int status, int[] payload) {
        // Step counter: latency (4 bytes) + steps (2 bytes)
        long latency = ((payload[9] & 0xFFL) << 24) | ((payload[8] & 0xFFL) << 16)
                | ((payload[7] & 0xFFL) << 8) | (payload[6] & 0xFFL);
        int steps = ((payload[11] << 8) & 0xFFFF) | (payload[10] & 0xFF);
        return new StepCounterEvent(status, timeStamp, steps, latency);
    }

    private DataEvent3f createActivityClassifierEvent(long timeStamp, int status, int[] payload) {
        int mostLikely = payload[6] & 0xFF;
        int[] confidences = new int[10];
        for (int i = 0; i < 10 && (7 + i) < payload.length; i++) {
            confidences[i] = payload[7 + i] & 0xFF;
        }
        return new ActivityClassifierEvent(status, timeStamp, mostLikely, confidences);
    }

    private DataEvent3f createVectorEvent(DataEventType type, long timeStamp, int... data) {
        if (data == null || data.length < 6) {
            return EMPTY_EVENT;
        }
        final int status = data[0] & 0xFFFF;
        final int x = data[1] & 0xFFFF;
        final int y = data[2] & 0xFFFF;
        final int z = data[3] & 0xFFFF;
        final int qReal = data[4] & 0xFFFF;
        final int qRadianAccuracy = data[5] & 0xFFFF;
        final Tuple3f tuple3f = ShtpUtils.createTupleFromFixed(type.getQ(), x, y, z);

        return new VectorEvent(type, status, tuple3f, timeStamp, intToFloat(qReal, type.getQ()),
                intToFloat(qRadianAccuracy, type.getQ()));
    }

    private DataEvent3f createDataEvent(DataEventType type, long timeStamp, int... data) {
        if (data == null || data.length < 4) {
            return EMPTY_EVENT;
        }
        final int status = data[0] & 0xFFFF;
        final int x = data[1] & 0xFFFF;
        final int y = data[2] & 0xFFFF;
        final int z = data[3] & 0xFFFF;
        final Tuple3f tuple3f = ShtpUtils.createTupleFromFixed(type.getQ(), x, y, z);
        return new DataEvent3f(type, status, tuple3f, timeStamp);
    }

    private ShtpPacketRequest createFeatureRequest(SensorReportId report, int timeBetweenReports, int specificConfig) {
        final long microsBetweenReports = timeBetweenReports * 1000L;
        final ShtpPacketRequest request = prepareShtpPacketRequest(ShtpChannel.CONTROL, 17);

        int[] packetBody = new int[request.getBodySize()];
        packetBody[0] = ControlReportId.SET_FEATURE_COMMAND.getId();
        packetBody[1] = report.getId();
        packetBody[2] = 0; // Change sensitivity (LSB)
        packetBody[3] = 0; // Change sensitivity (MSB)
        packetBody[4] = (int) microsBetweenReports & 0xFF;
        packetBody[5] = (int) (microsBetweenReports >> 8) & 0xFF;
        packetBody[6] = (int) (microsBetweenReports >> 16) & 0xFF;
        packetBody[7] = (int) (microsBetweenReports >> 24) & 0xFF;
        packetBody[8] = 0;  // Batch Interval (LSB)
        packetBody[9] = 0;  // Batch Interval
        packetBody[10] = 0; // Batch Interval
        packetBody[11] = 0; // Batch Interval (MSB)
        packetBody[12] = specificConfig & 0xFF;
        packetBody[13] = (specificConfig >> 8) & 0xFF;
        packetBody[14] = (specificConfig >> 16) & 0xFF;
        packetBody[15] = (specificConfig >> 24) & 0xFF;
        request.addBody(packetBody);
        return request;
    }

    private ShtpOperation getInitSequence(ShtpPacketRequest initRequest) {
        ShtpOperationResponse advResponse = new ShtpOperationResponse(ShtpChannel.COMMAND, 0);
        ShtpOperation headAdvertisementOp = new ShtpOperation(initRequest, advResponse);
        ShtpOperationBuilder builder = new ShtpOperationBuilder(headAdvertisementOp);

        ShtpOperationResponse reportIdResponse = new ShtpOperationResponse(ControlReportId.PRODUCT_ID_RESPONSE);
        ShtpOperation productIdOperation = new ShtpOperation(getProductIdRequest(), reportIdResponse);
        builder.addOperation(productIdOperation);

        ShtpOperationResponse resetResponse = new ShtpOperationResponse(ControlReportId.COMMAND_RESPONSE);
        ShtpOperation resetOperation = new ShtpOperation(null, resetResponse);
        builder.addOperation(resetOperation);

        return builder.build();
    }

    private ShtpOperation softResetSequence() {
        ShtpPacketRequest request = getSoftResetPacket();
        return getInitSequence(request);
    }

    private boolean softReset() {
        try {
            ShtpOperation opHead = softResetSequence();
            return processOperationChainByHead(opHead);
        } catch (InterruptedException | IOException e) {
            LOGGER.error("softReset error:{}", e.getMessage(), e);
        }
        return false;
    }

    private boolean initiate() {
        ShtpOperation opHead = getInitSequence(null);
        try {
            active.set(processOperationChainByHead(opHead));
        } catch (InterruptedException | IOException e) {
            throw new IllegalStateException("Problem initializing device!");
        }
        return active.get();
    }

    private boolean waitForLatch(CountDownLatch latch) {
        try {
            return latch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn("waitForLatch e: {}", e.getMessage());
            return false;
        }
    }

    private void initAndActive(final CountDownLatch latch, SensorReportId report, int reportPeriod) {
        executor.submit(() -> {
            boolean initState = initiate();
            if (initState && enableSensorReport(report, reportPeriod)) {
                latch.countDown();
                ready.set(initState);
            }
        });
    }

    private void reactivate(final CountDownLatch latch, SensorReportId report, int reportPeriod) {
        executor.submit(() -> {
            try {
                ShtpOperation opHead = getInitSequence(null);
                active.set(processOperationChainByHead(opHead));
                if (active.get() && enableSensorReport(report, reportPeriod)) {
                    latch.countDown();
                }
            } catch (InterruptedException | IOException e) {
                throw new IllegalStateException("not activated");
            }
        });
    }

    private void executeListenerJob() {
        executor.execute(() -> {
            if (ready.get()) {
                active.set(ready.get());
                while (active.get()) {
                    forwardReceivedPacketToListeners();
                }
            } else {
                throw new IllegalStateException("not initiated");
            }
        });
    }

    private void forwardReceivedPacketToListeners() {
        DataEvent3f deviceEvent = processReceivedPacket();
        if (!deviceEvent.getType().equals(DataEventType.NONE)) {
            for (DataListener l : listeners) {
                l.onResponse(deviceEvent);
            }
        }
    }

    private void awaitTermination() {
        try {
            var terminated = executor.awaitTermination(AWAIT_TERMINATION, TimeUnit.MILLISECONDS);
            if (!terminated) {
                LOGGER.warn("reached termination timeout");
                executor.shutdown();
            }
        } catch (InterruptedException e) {
            LOGGER.error("awaitTermination e: {}", e.getMessage(), e);
        }
    }
}
