package pl.sim.backend;

import pl.simNG.*;

public class DeltaForce extends SimGroup {

    public DeltaForce() {
        super("Delta Force", new SimPosition(20,20));


        SimUnit unit = new Abrams(5);
        this.addUnit(unit);

        this.route.add(SimVector2i.RIGHT);
        this.route.add(SimVector2i.RIGHT);
        this.route.add(SimVector2i.RIGHT);
        this.route.add(SimVector2i.RIGHT);
        this.route.add(SimVector2i.RIGHT);
        this.route.add(SimVector2i.DOWN);
        this.route.add(SimVector2i.DOWN);
        this.route.add(SimVector2i.RIGHT);
        this.route.add(SimVector2i.DOWN);
        this.route.add(SimVector2i.LEFT);
        this.route.add(SimVector2i.LEFT);
        this.route.add(SimVector2i.LEFT);
        this.route.add(SimVector2i.LEFT);
        this.route.add(SimVector2i.LEFT);

        //addProcess("move", this::move, 1);
        addTask(this::move,1);
    }

    @Override
    public void init() {

    }

    @Override
    public void move(){
        SimVector2i direction = route.poll();
        if (direction != null){
            this.position.add(direction);
            addTask(this::move,1);
        }
//        System.out.println(this.position);
    }

    @Override
    public void apply_damage(SimGroup self, SimBullet bullet) {

    }


}
