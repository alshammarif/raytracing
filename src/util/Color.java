package util;

public class Color {
    float r,g,b;

    public Color()
    {
        this.r = 0.0f;
        this.g = 0.0f;
        this.b = 0.0f;
    }

    public Color(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void addColor(float r, float g, float b)
    {
        this.r+=r;
        this.g+=g;
        this.b+=b;
    }

    public int toInt()
    {
        return (int)(r*255)<<16 | (int)(g*255)<<8 | (int)b*255;
    }

}
