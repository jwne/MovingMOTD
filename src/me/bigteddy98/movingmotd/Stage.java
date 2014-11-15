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

public enum Stage {
	HANDSHAKE(0), STATUS(1), LOGIN(2);

	private final int id;

	private Stage(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	// performance boost
	private final static Stage[] stages = new Stage[3];
	static {
		for (Stage stage : values()) {
			stages[stage.getId()] = stage;
		}
	}

	public static Stage fromId(int id) {
		return stages[id];
	}
}
