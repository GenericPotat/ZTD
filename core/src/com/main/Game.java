package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Random;

public class Game {
    //GAME VARIABLES
    Random r;

    //CONTROL VARIABLES
    boolean pause = false;

    String buildType = "beans";
    Button prevSelect;

    // GAME LISTS
    static ArrayList<Zombie> zombies = new ArrayList<Zombie>();
    static ArrayList<Cannon> cannons = new ArrayList<Cannon>();
    static ArrayList<Button> buttons = new ArrayList<Button>();
    static ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    static ArrayList<Effect> effects = new ArrayList<Effect>();
    static ArrayList<Wall> walls = new ArrayList<Wall>();

    Game(){
        r = new Random();
        setup();
    }

    void update(){
        spawn_zombies();
        if(!pause) {
            for (Zombie z : zombies) z.update();
            for (Cannon c : cannons) c.update();
            for (Button b : buttons) b.update();
            for (Bullet b : bullets) b.update();
            for (Wall w : walls) w.update();
        }
        for (Effect e : effects) e.update();
        housekeeping(); //last in update
        Main.lose = !(UI.life > 0);


    }

    void tap(int x, int y){
        effects.add(new Effect("click", x, y));
        for(Button b : buttons) {
            if (b.gethitbox().contains(x, y)) {
                //if button is unlocked
                if(b.type.equals("pause") || (b.type.equals("play"))) {
                    pause = !pause;

                    b.type = pause ? "play" : "pause";

                    System.out.println("YOU CLICKED PAUSE");
                    return;
                }
                if (!b.locked) {
                    //setting the created cannon
                    System.out.println(b.type);
                    buildType = b.type;
                    //doing the selection box\
                    hideselect();
                    //if (!(prevSelect == null)) prevSelect.selected = false;
                    prevSelect = b;
                    b.selected = true;


                }
                //if button is locked
                else {
                    if (b.t.hidden) {
                        hidett();
                        //if (!(prevSelect == null)) prevSelect.t.hidden = true;
                        prevSelect = b;

                        b.t.hidden = false;


                    } else {
                        if(UI.money >= (Tables.balance.get("unlock_" + b.type) == null ? 0 : (Tables.balance.get("unlock_"+b.type))))
                            UI.money -= (Tables.balance.get("unlock_" + b.type) == null ? 0 : (Tables.balance.get("unlock_"+b.type)));
                        else return;
                        b.locked = false;
                        b.t.hidden = true;

                    }

                }

                return;
            } else {

                if (b.t.close.gethitbox().contains(x, y) && !b.t.hidden) {hidett(); return;};
                if (b.t.gethitbox().contains(x, y) && !b.t.hidden) return;
                if (!b.t.gethitbox().contains(x, y) && !b.t.hidden) { hidett();}


            }
        }


        if(walls.size() < 3 && (buildType.equals("wall") || buildType.equals("mounted"))) {
            walls.add(new Wall(x, 0, buildType.equals("mounted")));
            return;
        }
        for(Cannon c : cannons) if(c.gethitbox().contains(x, y)) return;
        if(buildable(x, y)) if(UI.money >= (Tables.balance.get("cost_"+buildType) == null ? 10 : Tables.balance.get("cost_"+buildType))) {
            UI.money -= (Tables.balance.get("cost_"+buildType) == null ? 10 : Tables.balance.get("cost_"+buildType));
            cannons.add(new Cannon(buildType, x, y));
            //System.out.println(prevSelect);
        }
    }


    void draw(SpriteBatch batch){
        update();
        batch.draw(Resources.bg, 0, 0);
        UI.draw(batch);
        for (Zombie z: zombies) z.draw(batch);
        for (Cannon c: cannons) c.draw(batch);
        for (Button b: buttons) b.draw(batch);
        for (Bullet b: bullets) b.draw(batch);
        for (Effect e: effects) e.draw(batch);
        for (Wall w: walls) w.draw(batch);
    }

    void hidett(){
        for (Button b : buttons) b.t.hidden = true;
    }
    void hideselect(){
        for (Button b : buttons) b.selected = false;
    }


    //alternative method to my current prevSelect method
    //void deselect(){
    //	for(Button b : buttons) b.selected = false;
    //}

    //second part:
    //deselect();
    //b.selected = true;
    //current_type (something else for me) = b.type;

    boolean buildable(int x, int y){
        return (x < 1000 && ((y < 200 || y > 300) && y < 500 ));
    }

    void setup() {
        //Prepare Game
        UI.money = 10000;
        UI.life = 15;
        UI.score = 0;
        UI.wave = 0;

        //clear lists
        buttons.clear();
        bullets.clear();
        zombies.clear();
        walls.clear();
        effects.clear();
        cannons.clear();

        //init all the tables
        Tables.init();

        //make some buttons
        buttons.add(new Button("cannon", buttons.size() * 75 + 200, 525));
        buttons.get(buttons.size() - 1).locked = false;
        buttons.get(buttons.size() - 1).selected = true;

        buttons.add(new Button("double", buttons.size() * 75 + 200, 525));
        buttons.add(new Button("super", buttons.size() * 75 + 200, 525));
        buttons.add(new Button("fire", buttons.size() * 75 + 200, 525));
        buttons.add(new Button("laser", buttons.size() * 75 + 200, 525));
        buttons.add(new Button("wall", buttons.size() * 75 + 200, 525));
        buttons.get(buttons.size() - 1).locked = false;
        buttons.get(buttons.size() - 1).selected = false;
        buttons.add(new Button("mounted", buttons.size() * 75 + 200, 525));


        //pause button
        buttons.add(new Button("pause", 1024 - 75, 525));
        buttons.get(buttons.size() - 1).locked = false;
        buttons.get(buttons.size() - 1).selected = false;

    }

    void housekeeping() {
        for (Zombie z : zombies)
            if (!z.active) {

                effects.add(new Effect("blood", z.x, z.y));
                zombies.remove(z);
                break;
            }
        for (Bullet b : bullets)
            if (!b.active) {
                bullets.remove(b);
                break;
            }
        for (Effect e : effects)
            if (!e.active) {
                effects.remove(e);
                break;
            }
        for (Cannon c : cannons)
            if (!c.active) {
                cannons.remove(c);
                break;
            }
        for (Wall w : walls)
            if (!w.active) {
                walls.remove(w);
                break;
            }
    }





    void spawn_zombies() {
        if(!zombies.isEmpty()) return;
        UI.wave++;
        for(int i = 0; i < 5 * UI.wave; i++){
            switch(r.nextInt(10)){

                case 0: case 1: case 2:
                    zombies.add(new Zombie("zzz", 1024 + (i * 50), r.nextInt(450)));
                    break;
                case 3: case 4:
                    zombies.add(new Zombie("fast", 1024 + (i * 50), r.nextInt(450)));
                    break;
                case 5:
                    zombies.add(new Zombie("riot", 1024 + (i * 50), r.nextInt(450)));
                    break;
                default:
                    zombies.add(new Zombie("dif", 1024 + (i * 50), r.nextInt(450)));
                    break;

            }
        }

    }

}
