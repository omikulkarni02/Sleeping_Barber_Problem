import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
public class SleepingBarber {
    public static void main(String a[])
    {
    	System.out.println("Opening Shop");
    	//barbers: 3
    	int totalBarber = 3, customerID = 1;
    	Random r = new Random();
    	//mean: 500, std_dev: 100
        double val = r.nextGaussian() * 100+ 500;
        int millisDelay = (int) Math.round(val);
        ExecutorService executor =  Executors.newFixedThreadPool(10);
    	
    	BarberShop shop = new BarberShop(totalBarber);
 
        for(int i = 1; i <= totalBarber; i++) 
        {
	        Barber barber = new Barber(shop, i); 
	        Thread barberthread = new Thread(barber);
	     	executor.execute(barberthread);
        }
        
        for(int i=0;i<20;i++)
        {
          Customer customer = new Customer(shop);
          Thread customerthread = new Thread(customer);
          customer.setName(customerID++);
          executor.execute(customerthread);
          try
          {
          	Thread.sleep(millisDelay);
          }
          catch(InterruptedException iex)
          {
              iex.printStackTrace();
          }
        }
       
        executor.shutdown();
    }
}
 
class Barber implements Runnable
{
	BarberShop shop;
    int barber_id;
    public Barber(BarberShop shop,int id)
    {
        this.shop = shop;
        barber_id= id;
    }
    public void run()
    {
        System.out.println("Barber "+ barber_id+ " entered shop");
        while(true)
        {
            shop.cutHair(barber_id);
        }
    }
}


class Customer implements Runnable
{
	
    int name;
    Barber barber;
    BarberShop shop;
    int cnt;
    public Customer(BarberShop shop)
    {
        this.shop = shop;
        cnt = shop.barber_cnt;
    }
 
    public int getName() 
    {
        return name;
    }

    public void setName(int name) 
    {
        this.name = name;
    }
    
    public void run()
    {
    	goForHairCut();
    }
    private synchronized void goForHairCut()
    {
        shop.addCustomers(this);
    }
}

 
class BarberShop
{
	Random r = new Random();
	Barber barber;
	int available_barber= 1;
    int nchair;
    List<Integer> listCustomer;
    SleepingBarber sb;
    
    int barber_cnt;

    public BarberShop(int barber_cnt)
    {
        nchair = 3;
        listCustomer = new LinkedList<Integer>();
        this.barber_cnt = barber_cnt;
        available_barber = barber_cnt;
    }
 
    public void cutHair(int id)
    {
        int customer;
        
        synchronized (listCustomer)
        {
 
            while(listCustomer.size()==0)
            {
                System.out.println("Barber "+ id+ " is waiting for customer and sleeping in his chair");
                try
                {
                	//barber waiting for customer to arrive
                	listCustomer.wait();
                }
                catch(InterruptedException iex)
                {
                    iex.printStackTrace();
                }
            }
            //taking 1st customer from the the customer list
            customer = (Integer)((LinkedList<?>)listCustomer).poll();
        }
        
        
        //mean: 2000, std_dev: 500
        double val = r.nextGaussian() * 500 + 2500;
        int millisDelay = (int) Math.round(val);
        try
        {   
        	//decrease count of barber
        	available_barber--;
            System.out.println("Barber " + id +" is cuting hair of Customer "+customer);
            Thread.sleep(millisDelay);
            System.out.println("Barber "+id+" completed Cuting hair of customer "+customer);
            System.out.println("Barber "+id+" holding door for customer "+customer+" to leave the shop");
            //barber waits for customer to leave the shop
            Thread.sleep(500);
            //increment the count of barber
            available_barber++;
            if(listCustomer.size()>0)
            {
            	System.out.println("Barber "+ id+" is now free and calls next customer from waiting room");
            }
        }
        catch(InterruptedException iex)
        {
            iex.printStackTrace();
        }
        
    }
 
    public void addCustomers(Customer customer)
    {
    	//customer generated
       System.out.println("Customer "+customer.getName()+" enters the salon");
       
       synchronized (listCustomer) 
       {
    	   //check availability of chairs in waiting room
    	   if(listCustomer.size() == nchair)
           {
               System.out.println("No chair available for customer "+customer.getName());
               System.out.println("Customer "+customer.getName()+" leaves the shop");
               return ;
           }
    	   
    	   //check barbers status
    	   if(available_barber==0)
           {
    		   System.out.println("Customer "+ +customer.getName()+" looks for barber, but all barbers are busy");
    		   System.out.println("Number of seats available in waiting room: "+ (nchair-listCustomer.size()));
    		   System.out.println("Customer "+customer.getName()+ " got the chair in waiting area");
           }
    	   else {
    		   System.out.println("Customer "+customer.getName()+ " finds barber available");
    	   }
    	   
    	   //add customer to the list
    	   ((LinkedList<Integer>)listCustomer).offer(customer.getName());
    	   
    	   if(listCustomer.size()>0)
           {
    		   //notify barber that customer has arrived
    		   listCustomer.notify();
           } 
       }
    }
    
}