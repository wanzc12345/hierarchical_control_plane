import java.io.IOException;


public class startTreeController {

	public static void main(String[] args) throws IOException {
		//main access
		if(args.length==1){
			ControllerNode cn = new ControllerNode(args[0]);
			cn.run();
		}else if(args.length==0){
			ControllerNode cn = new ControllerNode();
			cn.run();
		}else{
			System.out.println("Wrong parameters!");
		}
	}

}
