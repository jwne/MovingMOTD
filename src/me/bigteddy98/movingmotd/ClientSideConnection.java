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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;

public class ClientSideConnection extends ChannelHandlerAdapter {

	private final NetworkManager networkManager;
	private final String toHostname;
	private final int toPort;

	public volatile Channel outgoingChannel;

	public ClientSideConnection(NetworkManager networkManager, String hostname, int toPort) {
		this.networkManager = networkManager;
		this.toHostname = hostname;
		this.toPort = toPort;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.networkManager.incomingChannel = ctx.channel();
		this.networkManager.clientsidePipeline = ctx.pipeline();

		Bootstrap bootstrab = new Bootstrap();
		bootstrab.group(networkManager.incomingChannel.eventLoop());
		bootstrab.channel(ctx.channel().getClass());
		bootstrab.handler(new ServerSideConnectionInitialization(networkManager));
		bootstrab.option(ChannelOption.AUTO_READ, false);
		ChannelFuture f = bootstrab.connect(this.toHostname, this.toPort);
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					networkManager.incomingChannel.read();
				} else {
					networkManager.incomingChannel.close();
				}
			}
		});
		this.outgoingChannel = f.channel();
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		this.outgoingChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					ctx.channel().read();
				} else {
					future.channel().close();
				}
			}
		});
	}
}
