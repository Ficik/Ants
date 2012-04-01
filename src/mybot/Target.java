package mybot;

public class Target {

	private int assignees = 0;
	
	public boolean isAssigned(){
		return assignees>0;
	}
	
	public void assign(){
		assignees +=1;
	}
	
	public void unassign(){
		if (assignees != 0)
			assignees -=1;
	}
	
}
