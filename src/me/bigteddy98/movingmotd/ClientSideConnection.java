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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

	private Stage stage = Stage.HANDSHAKE;

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf clonedBuf = Unpooled.copiedBuffer((ByteBuf) msg);
		ByteBuf originalBuf = (ByteBuf) msg;

		int packetSize = PacketUtils.readVarInt(originalBuf);
		if (originalBuf.readableBytes() < packetSize) {
			System.out.println("Packet was smaller than the lenght.");
		}
		int id = PacketUtils.readVarInt(originalBuf);
		if (this.stage == Stage.HANDSHAKE) {
			if (id != 0x00) {
				System.out.println("Handshake ID was not equal to 0x00.");
			}
			// followed by varint, string, unsigned short and another varint
			PacketUtils.readVarInt(originalBuf);
			PacketUtils.readString(originalBuf);
			originalBuf.readUnsignedShort();
			int nextStage = PacketUtils.readVarInt(originalBuf);
			this.stage = Stage.fromId(nextStage);
		} else if (this.stage == Stage.LOGIN) {
			// just sent it
		} else if (this.stage == Stage.STATUS) {
			// TODO someone is pinging!
			
			
			
			originalBuf.release();
			return;
		}
		originalBuf.release();
		this.outgoingChannel.writeAndFlush(clonedBuf).addListener(new ChannelFutureListener() {
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
