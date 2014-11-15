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

import java.io.UnsupportedEncodingException;

public class PacketUtils {

	public static int readVarInt(ByteBuf buf) {
		int out = 0;
		int bytes = 0;
		byte in;
		while (true) {
			in = buf.readByte();
			out |= (in & 0x7F) << (bytes++ * 7);
			if (bytes > 5) {
				throw new RuntimeException("VarInt too big");
			}
			if ((in & 0x80) != 0x80) {
				break;
			}
		}
		return out;
	}

	public static void writeVarInt(ByteBuf buf, int value) {
		int part;
		while (true) {
			part = value & 0x7F;
			value >>>= 7;
			if (value != 0) {
				part |= 0x80;
			}
			buf.writeByte(part);
			if (value == 0) {
				break;
			}
		}
	}

	public static String readString(ByteBuf buf) {
		int len = readVarInt(buf);
		byte[] b = new byte[len];
		buf.readBytes(b);
		try {
			return new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("No UTF-8 support? This platform is not supported!", e);
		}
	}

	public static void writeString(ByteBuf buf, String s) {
		byte[] b = null;
		try {
			b = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("No UTF-8 support? This platform is not supported!", e);
		}
		writeVarInt(buf, b.length);
		buf.writeBytes(b);
	}
}
