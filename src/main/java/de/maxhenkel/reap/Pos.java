package de.maxhenkel.reap;

import java.util.List;

import net.minecraft.util.math.BlockPos;

public class Pos {

	private int x, y, z;

	public Pos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Pos(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public Pos add(int x, int y, int z){
		return new Pos(x+this.x, y+this.y, z+this.z);
	}
	
	public BlockPos getBlockPos(){
		return new BlockPos(this.x, this.y, this.z);
	}

	@Override
	public String toString() {
		return "Pos [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Pos){
			Pos p=(Pos) obj;
			if(p.getX()==this.x&&p.getY()==this.y&&p.getY()==this.y){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean contains(List<Pos> list){
		for(Pos p:list){
			if(p.equals(this)){
				return true;
			}
		}
		return false;
	}
	
}
