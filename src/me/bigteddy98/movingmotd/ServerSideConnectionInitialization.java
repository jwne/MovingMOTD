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
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ServerSideConnectionInitialization extends ChannelInitializer<SocketChannel> {

	private final String toHost;
	private final int toPort;

	public ServerSideConnectionInitialization(String toHost, int toPort) {
		this.toHost = toHost;
		this.toPort = toPort;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipe = channel.pipeline();
		pipe.addLast("proxy_forward", new ServerSideConnection());
	}
}
