package edu.columbia.cs6998.sdn.project;



public class SwitchIdToLong {
	
	public static long SidToLong(String sid) {
		if (sid.length() != 23) return -1;
		int sec = 0;
		int index = 0;
		long rst = 0;
		
		while(sec < 8) {
			int pos = 3 * sec + index;
			
			rst = rst * 16 + charToLong(sid.charAt(pos));
			if (index == 0) index++;
			else if (index == 1) {
				index = 0;
				sec++;
			}
			
		}
		
		return rst;
	}
	
	public static long charToLong(char c) {
		long rst = 10;
		switch(c) {
			case '0' : rst = 0; break;
			case '1' : rst = 1; break;
			case '2' : rst = 2; break;
			case '3' : rst = 3; break;
			case '4' : rst = 4; break;
			case '5' : rst = 5; break;
			case '6' : rst = 6; break;
			case '7' : rst = 7; break;
			case '8' : rst = 8; break;
			case '9' : rst = 9; break;
			case 'a' : rst = 10; break;
			case 'b' : rst = 11; break;
			case 'c' : rst = 12; break;
			case 'd' : rst = 13; break;
			case 'e' : rst = 14; break;
			case 'f' : rst = 15; break;
			
		}
		return rst;
	}
	
	public static void main(String[] arg) {
		String a = "00:00:00:00:00:00:01:21";
		System.out.println(SidToLong(a));
	}
}
