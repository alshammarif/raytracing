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
        if(this.r>1)
            this.r =1;
        this.g+=g;
        if(this.g>1)
            this.r =1;
        this.b+=b;
        if(this.b>1)
            this.b=1;
    }

    public void mul(float r, float g, float b)
    {
        this.r*=r;
        if(this.r>1)
            this.r =1;
        this.g*=g;
        if(this.g>1)
            this.r =1;
        this.b*=b;
        if(this.b>1)
            this.b=1;
        if(this.r<0)
            this.r= 0;
        if(this.g<0)
            this.g=0;
        if(this.b<0)
            this.b=0;
    }

    public int toInt()
    {
        return (int)(r*255)<<16 | (int)(g*255)<<8 | (int)b*255;
    }

}
