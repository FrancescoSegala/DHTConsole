package DhtUtils;



public class testAPI {
    public static void main(String[] args) {

        String command = "";
        for (int i = 0 ; i< args.length ; i++){
          command+= args[i] + " ";
        }
        try {
          System.out.println(""+ command.toString() );

            DhtAPI api = new DhtAPI();
            String res = api.findNode( command );
            System.out.println(res);

        } catch (Exception e) {
            e.printStackTrace();
        }





    }
}
