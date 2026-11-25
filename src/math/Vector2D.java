package math;

public class Vector2D {
    private double x,y;
    
    public Vector2D(double x,double y){
        this.x=x;
        this.y=y;
    }
    public Vector2D(){
        x=0;
        y=0;
    }
    public Vector2D add(Vector2D v){
        return new Vector2D(x+v.getX(),y+v.getY());
    }
    public Vector2D substract(Vector2D v){
        return new Vector2D(x-v.getX(),y-v.getY());
    }
    public Vector2D scale(double valor){
        return new Vector2D(x*valor,y*valor);
    }
    public double getMagni(){
        return Math.sqrt(x*x+y*y);
    }
    public Vector2D limitev(double valor){
        if(getMagni()>valor){
            return this.normalizar().scale(valor);
        }
        return this;
    }
    public Vector2D normalizar(){
        double magni=getMagni();
        return new Vector2D(x/magni,y/magni);
    }
    public Vector2D setDireccion(double angle){
        double magni=getMagni();
        return new Vector2D(Math.cos(angle)*magni,Math.sin(angle)*magni);
    }
    public double getX() {
        return x;
    }
    public void setX(double x){
        this.x=x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y){
        this.y=y;
    }
}
