/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.asdec.tags;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.jpexs.asdec.SWFInputStream;

public class FrameLabel extends Tag {

	private String name;

	public FrameLabel(byte[] data, int version, long pos) throws IOException {
		super(43, data, pos);
		SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        name = sis.readString();
	}

	@Override
	public String toString() {
		return "FrameLabel";
	}
}