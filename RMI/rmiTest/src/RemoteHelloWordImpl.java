import java.rmi.RemoteException;

public class RemoteHelloWordImpl  implements RemoteHelloWord{
    @Override
    public String sayHello() throws RemoteException {
        return "Hello Word!";
    }
}
