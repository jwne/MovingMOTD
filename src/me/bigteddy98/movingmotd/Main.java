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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ThreadFactory;

public class Main implements Runnable {

	public static final int MAX_NETTY_BOSS_THREADS = 2;
	public static final int MAX_NETTY_WORKER_THREADS = 4;
	public static Main instance;

	public static Main getInstance() {
		return instance;
	}

	public Main() {
		instance = this;
	}

	public static void main(String[] args) {
		new Main().run();
	}

	public int fromPort = 25565;
	public int toPort = 25566;
	public ThreadGroup nettyThreadGroup;
	
	@Override
	public void run() {
		nettyThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "NettyThreadGroup");
		new Thread(this.nettyThreadGroup, new Runnable() {

			@Override
			public void run() {
				final ThreadGroup group = new ThreadGroup(Main.this.nettyThreadGroup, "PortListener");
				EventLoopGroup bossGroup = new NioEventLoopGroup(MAX_NETTY_BOSS_THREADS, new ThreadFactory() {

					private int counter = 0;
					private String newName = group.getName() + "\\nettyboss";

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(group, r, newName + "\\" + counter++);
						t.setPriority(Thread.NORM_PRIORITY - 1);
						t.setDaemon(true);
						return t;
					}
				});
				EventLoopGroup workerGroup = new NioEventLoopGroup(MAX_NETTY_WORKER_THREADS, new ThreadFactory() {

					private int counter = 0;
					private String newName = group.getName() + "\\nettyworker";

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(group, r, newName + "\\" + counter++);
						t.setPriority(Thread.NORM_PRIORITY - 1);
						t.setDaemon(true);
						return t;
					}
				});
				try {
					ServerBootstrap bootstrab = new ServerBootstrap();
					bootstrab.group(bossGroup, workerGroup);
					bootstrab.channel(NioServerSocketChannel.class);
					bootstrab.childHandler(new ClientSideConnectionInitialization("localhost", toPort));
					bootstrab.childOption(ChannelOption.AUTO_READ, false);
					bootstrab.bind(fromPort).sync().channel().closeFuture().sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			}
		}).start();
	}
}
