/* 
 * MovingMOTD
 * Copyright (C) 2014 Sander Gielisse
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.bigteddy98.movingmotd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketBuilder {
	
	public static ByteBuf buildPingResponse(String jsonResponse) {
		ByteBuf newBuf = Unpooled.buffer();
		PacketUtils.writeVarInt(newBuf, 0x00);
		PacketUtils.writeString(newBuf, jsonResponse);
		return wrapSize(newBuf);
	}

	public static ByteBuf buildPingTime(long time) {
		ByteBuf newBuf = Unpooled.buffer();
		PacketUtils.writeVarInt(newBuf, 0x01);
		newBuf.writeLong(time);
		return wrapSize(newBuf);
	}

	private static ByteBuf wrapSize(ByteBuf buf) {
		ByteBuf newBuf = Unpooled.buffer();
		PacketUtils.writeVarInt(newBuf, buf.readableBytes());
		newBuf.writeBytes(buf);
		buf.release();
		return newBuf;
	}
}
