package com.tomi.tivi;

import android.hardware.Camera.Size;

public class SizeHolder {
	Size size;
	
	public SizeHolder(Size size){
		this.size = size;
	}
	
	public String toString() {
		StringBuffer rst = new StringBuffer();
		rst.append(size.width);
		rst.append("x");
		rst.append(size.height);
		return rst.toString();
	}
}
