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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ClientSideConnectionInitialization extends ChannelInitializer<SocketChannel> {

	private final String hostname;
	private final int toPort;
	private final NetworkManager networkManager;

	public ClientSideConnectionInitialization(String hostname, int toPort) {
		this.hostname = hostname;
		this.toPort = toPort;
		this.networkManager = new NetworkManager(hostname, toPort);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

	}
}
