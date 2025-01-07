package pl.sim.backend;

import pl.simNG.*;

import java.util.Random;

public class RandomForce extends SimGroup {

    Random random = new Random();

    public RandomForce(String name, SimPosition position, SimForceType forceType) {
        super(name, position, forceType);


        SimUnit unit = new Abrams(5);
        this.addUnit(unit);

//        List<SimVector2i> directions = new ArrayList<>();
//        directions.add(SimVector2i.UP);
//        directions.add(SimVector2i.DOWN);
//        directions.add(SimVector2i.LEFT);
//        directions.add(SimVector2i.RIGHT);

//        int length = 1000;
//        for (int i = 0; i < length; i++) {
//            SimVector2i randomDirection = directions.get(random.nextInt(directions.size()));
//            this.route.add(randomDirection);
//        }

    }

    @Override
    public void init(){
        this.route = calculateRouteTo(new SimPosition(2,2));

        addTask(this::move,1);
        addProcess("shot", this::shot, 5);
    }

    @Override
    public void move(){

        SimVector2i direction = route.poll();
        if (direction != null){
            this.position.add(direction);
            addTask(this::move,(random.nextInt()%5)+5);
        }
//        System.out.println(this.position);
    }

    @Override
    public void apply_damage(SimGroup attacker, SimBullet bullet) {
        if (!units.isEmpty()) {
            SimUnit unit = units.get(0);
            unit.setAmount(unit.getAmount()-1);
            units.set(0, unit);
            this.cleanDestroyedUnits();
        }
    }

    public void shot(){
        if(!visibleGroups.isEmpty()){
            SimGroup group = visibleGroups.get(0);
            for(SimUnit unit: units){
                if (unit.inShotRange(group.getPosition())){
                    group.apply_damage(this, new TankShell());
                }
            }
        }
    }

}
