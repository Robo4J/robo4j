/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This QueueFIFOEntry.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.concurrency;

import java.util.concurrent.atomic.AtomicLong;

/**
 * keep order in the queue
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 13.04.2016
 */
public class QueueFIFOEntry<FIFOTransferType extends Comparable<FIFOTransferType>>
		implements Comparable<QueueFIFOEntry<FIFOTransferType>>, TransferSignal {

	private static final AtomicLong seq = new AtomicLong(0);
	private final long seqNum;
	private final FIFOTransferType entry;

	public QueueFIFOEntry(FIFOTransferType entry) {
		seqNum = seq.getAndIncrement();
		this.entry = entry;
	}

	public FIFOTransferType getEntry() {
		return entry;
	}

	public int compareTo(QueueFIFOEntry<FIFOTransferType> other) {
		int res = entry.compareTo(other.entry);
		if (res == 0 && other.entry != this.entry)
			res = (seqNum < other.seqNum ? -1 : 1);
		return res;
	}
}
