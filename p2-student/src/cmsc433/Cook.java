package cmsc433;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;

	/**
	 * You can feel free to modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while(true) {
				//YOUR CODE GOES HERE...

				//remove order from order queue
				Order order;
				synchronized(Simulation.orders) {
					while (Simulation.orders.isEmpty()) {
						Simulation.orders.wait();
					}
					
					order = Simulation.orders.remove(0);
					Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order.order, order.orderNumber));
				}
				

				//place each item in order into appropriate machine
				if (order != null) {
					
					Thread[] machines = new Thread[order.order.size()];
					int i = 0;
					
					for (Food item : order.order) {
						
						//call make food
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, item, order.orderNumber));

						if (item.equals(FoodType.wings)) {
							machines[i] = Simulation.fryer.makeFood();
						} else if (item.equals(FoodType.pizza)) {
							machines[i] = Simulation.oven.makeFood();
						} else if (item.equals(FoodType.sub)) {
							machines[i] = Simulation.grillPress.makeFood();
						} else if (item.equals(FoodType.soda)) {
							machines[i] = Simulation.fountain.makeFood();
						}
						
						i++;

					}
					
					for (int j = 0; j < i; j++) {
						machines[j].join();
						Simulation.logEvent(SimulationEvent.cookFinishedFood(this, order.order.get(j), order.orderNumber));
					}
					
					
					//once food is done notify customer
					synchronized(order) {
						Simulation.completed.add(order);
						order.notify();
						Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, order.orderNumber));
					}
				}
			}

		}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}
}
