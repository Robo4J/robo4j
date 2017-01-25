package com.robo4j.core.client.request;

import com.robo4j.core.client.enums.RequestStatusEnum;
import com.robo4j.core.client.enums.RequestUnitStatusEnum;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpMethod;
import com.robo4j.http.HttpVersion;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 24.01.2017
 */
public class RoboRequestProcessorCallable implements Callable<Object> {

    private static final String NEW_LINE = "\n";
    private static final int METHOD_KEY_POSITION = 0, URI_VALUE_POSITION = 1, VERSION_POSITION = 2, HTTP_HEADER_SEP = 9;
    private final RequestProcessorFactory processorFactory;
    private Socket connection;

    public RoboRequestProcessorCallable(Socket connection) {
        this.connection = connection;
        this.processorFactory = RequestProcessorFactory.getInstance();
    }

    @Override
    public RequestStatusEnum call() throws IOException {

        if (Objects.isNull(connection)) {
            // SimpleLoggingUtil.debug(RequestProcessorCallable.class, "ONLY
            // INNER AGENT ACTIVATION");
            processorFactory.activateInner();

        }
        // for security checks
        try (final Writer out = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
             final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

            final Map<String, String> tmpMap = new ConcurrentHashMap<>();
            boolean firstLine = true;
            String[] tokens = null;
            String method = null;
            String inputLine;

            while (!(inputLine = correctLine(in.readLine())).equals(ConstantUtil.EMPTY_STRING)) {
                if (firstLine) {
                    SimpleLoggingUtil.debug(getClass(), " inputLine: " + inputLine);
                    tokens = inputLine.split(ConstantUtil.HTTP_EMPTY_SEP);
                    method = tokens[METHOD_KEY_POSITION];
                    firstLine = false;
                } else {
                    final String[] array = inputLine.split(ConstantUtil.getHttpSeparator(HTTP_HEADER_SEP));
                    tmpMap.put(array[METHOD_KEY_POSITION], array[URI_VALUE_POSITION]);
                }
            }

            final HttpMethod httpMethod = HttpMethod.getByName(method);

            if (Objects.nonNull(httpMethod) && Objects.nonNull(tokens)) {
                final HttpMessage httpMessage = new HttpMessage(httpMethod, URI.create(tokens[URI_VALUE_POSITION]),
                        HttpVersion.getByValue(tokens[VERSION_POSITION]), tmpMap);

                ProcessorResult processorResult;
                switch (httpMethod) {
                    case GET:
                        processorResult = processorFactory.processGet(httpMessage);
                        if(processorResult.getStatus().equals(RequestUnitStatusEnum.STOP)){
                            result = RequestStatusEnum.EXIT;
                            SimpleLoggingUtil.debug(getClass(), "GET REQUEST EXIT SET");
                        } else {
                            result = convertToResult(processorResult);
                        }
                        out.write(processorResult.getMessage());
                        break;
                    case POST:
                        processorResult = processorFactory.processPost(httpMessage, in);
                        SimpleLoggingUtil.debug(getClass(), "POST: " + processorResult);

                        out.write("HTTP/1.1 200 OK\n");
                        Map<String, String> responseValues = new HashMap<>();
                        // responseValues.put("Transfer-Encoding", "UTF-8");
                        // responseValues.put("Connection", "closed");
                        // responseValues.put("Server", "Robo4j-brick");
                        // responseValues.put("Content-Type", "text/html");
                        responseValues.put("Content-Length", String.valueOf(processorResult.getMessage().length()));
                        responseValues.forEach((k, v) -> {
                            try {
                                out.write(k + ": " + v + NEW_LINE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        out.write(NEW_LINE);
                        out.write(processorResult.getMessage());
                        result = convertToResult(processorResult);
                        break;
                    case HEAD:
                    case PUT:
                        out.write("something more");
                        result = RequestStatusEnum.ACTIVE;
                        break;
                    case DELETE:
                    case CONNECT:
                    case TRACE:
                    case OPTIONS:
                    default:
                        processorResult = processorFactory.processDefault(httpMessage);
                        out.write(processorResult.getMessage());
                        result = convertToResult(processorResult);
                        break;
                }
                out.flush();
            }
            return result;
        } catch (IOException | InterruptedException ex) {
            SimpleLoggingUtil.print(RequestProcessorCallable.class,
                    "Error talking to " + connection.getRemoteSocketAddress() + " ex= " + ex);
        } finally {

            try {
                SimpleLoggingUtil.print(RequestProcessorCallable.class, "SOCKET CLOSED");

                connection.close();
            } catch (IOException e) {
                SimpleLoggingUtil.print(RequestProcessorCallable.class,
                        "Error Closing Connection to " + connection.getRemoteSocketAddress() + " : " + e);
            }
        }

        return result;


    //Private Methods
    private static String correctLine(String line) {
        return line == null ? "" : line;
    }

}
