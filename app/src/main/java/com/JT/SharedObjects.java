package com.JT;

public class SharedObjects
{
	public static SharedObjects mInstance;
	
	public SharedObjects getInstance(){
		if(mInstance == null){
			return new SharedObjects();
		}else{
			return mInstance;
		}
	}
}