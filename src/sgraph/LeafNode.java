package sgraph;

import com.jogamp.opengl.GL3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.*;
import util.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.awt.*;

/**
 * This node represents the leaf of a scene graph. It is the only type of node that has
 * actual geometry to render.
 * @author Amit Shesh
 */
public class LeafNode extends AbstractNode
{
    /**
     * The name of the object instance that this leaf contains. All object instances are stored
     * in the scene graph itself, so that an instance can be reused in several leaves
     */
    protected String objInstanceName;

    /**
     * A map of texture names and the texture-images
     */
    protected Map<String, TextureImage> textures;
    /**
     * The material associated with the object instance at this leaf
     */
    protected util.Material material;
    public util.Light light = null;

    public ArrayList<Light> lights;

    protected String textureName;

    protected Map<String,util.PolygonMesh> meshes;
    protected PolygonMesh currentMesh;

    protected Vector4f leftBotFront,leftTopFront, leftBotBack, leftTopBack;
    protected Vector4f rightBotFront,rightTopFront, rightBotBack, rightTopBack, center;//bounding box

    protected  Matrix4f thismodelView;

    public LeafNode(String instanceOf,IScenegraph graph,String name)
    {
        super(graph,name);
        this.objInstanceName = instanceOf;
        lights = new ArrayList<Light>();
        meshes = new HashMap<>();
        leftBotFront = new Vector4f();
        leftTopFront = new Vector4f();
        leftBotBack = new Vector4f();
        leftTopBack = new Vector4f();
        rightBotFront  = new Vector4f();
        rightTopFront  = new Vector4f();
        rightBotBack  = new Vector4f();
        rightTopBack  = new Vector4f();
        currentMesh = new PolygonMesh();
        center = new Vector4f();
        textures = new HashMap<>();
    }

    /*
	 *Set the material of each vertex in this object
	 */
    @Override
    public void setMaterial(util.Material mat)
    {
        material = new util.Material(mat);
    }

    /**
     * Set texture ID of the texture to be used for this leaf
     * @param name
     */
    @Override
    public void setTextureName(String name)
    {
        textureName = name;
    }

    /*
     * gets the material
     */
    public util.Material getMaterial()
    {
        return material;
    }

    @Override
    public INode clone()
    {
        LeafNode newclone = new LeafNode(this.objInstanceName,scenegraph,name);
        newclone.setMaterial(this.getMaterial());
        return newclone;
    }


    /**
     * Delegates to the scene graph for rendering. This has two advantages:
     * <ul>
     *     <li>It keeps the leaf light.</li>
     *     <li>It abstracts the actual drawing to the specific implementation of the scene graph renderer</li>
     * </ul>
     * @param context the generic renderer context {@link sgraph.IScenegraphRenderer}
     * @param modelView the stack of modelview matrices
     * @throws IllegalArgumentException
     */
    @Override
    public void draw(IScenegraphRenderer context,Stack<Matrix4f> modelView) throws IllegalArgumentException
    {
        // getting the textures
       //sgraph.GL3ScenegraphRenderer render = new sgraph.GL3ScenegraphRenderer();
       //this.textures = render.textures;
        if (objInstanceName.length()>0)
        {
            context.drawMesh(objInstanceName,material,textureName,modelView.peek());
        }
    }

    public void addLight(Light l)
    {
        lights.add(l);
    }

     public ArrayList<Light> getLights(Stack<Matrix4f> modelView) {
        ArrayList<Light> llights = new ArrayList<Light>();
        for(int i = 0; i < lights.size(); i++) {
            Light l = lights.get(i);
            l.setPosition(l.getPosition().mul(modelView.peek()));
            llights.add(l);
        }
        return llights;
    }

    public Vector4f getMinBounds()
    {
        return leftBotFront;
    }

    public Vector4f getMaxBounds()
    {
        return  rightTopBack;
    }

    public Vector4f getleftTopFront()
    {
        return  leftTopFront;
    }

    public Vector4f getleftBotBack()
    {
        return  leftBotBack;
    }

    public Vector4f getleftTopBack() { return leftTopBack;}

    public Vector4f getrightBotBack() { return rightBotBack;}

    public Vector4f getrightTopFront() {return rightTopFront;}

    public Vector4f getrightBotFront() {return rightBotFront; }

    @Override
    public void setBoundingBox()
    {
        currentMesh = meshes.get(objInstanceName);
        currentMesh.computeBoundingBox();
        leftBotBack = currentMesh.getMinimumBounds();
        rightTopFront = currentMesh.getMaximumBounds();
        leftBotFront = new Vector4f(leftBotBack.x, leftBotBack.y, rightTopFront.z,1);
        leftTopBack = new Vector4f(leftBotBack.x, rightTopFront.y, leftBotBack.z,1);
        leftTopFront = new Vector4f(leftBotBack.x,rightTopFront.y,rightTopFront.z,1);
        rightBotFront = new Vector4f(rightTopFront.x,leftBotBack.y,rightTopFront.z,1);
        rightTopBack = new Vector4f(rightTopFront.x, leftBotBack.y,leftBotBack.z, 1);
        rightTopBack = new Vector4f(rightTopFront.x,rightTopFront.y,leftBotBack.z, 1);
        Vector4f center = new Vector4f(
                (leftBotBack.x + rightTopFront.x)/2,
                (leftBotBack.y + rightTopFront.y)/2,
                (leftBotBack.z + rightTopFront.z)/2,
                1
        );
    }
    @Override
    public void setMeshes(Map<String,PolygonMesh> meshes)
    {
        this.meshes = meshes;
    }

    public Vector4f getCenter()
    {
        //Bottom face center
        Vector4f blc = setcenter(leftBotBack, leftBotFront);
        Vector4f brc = setcenter(rightBotFront, rightBotBack);
        Vector4f bot = setcenter(blc, brc);

        //Top face center
        Vector4f tlc = setcenter(leftTopBack, leftTopFront);
        Vector4f trc = setcenter(rightTopFront, rightTopBack);
        Vector4f top = setcenter(tlc, trc);

        //center of box
        Vector4f center = new Vector4f(
                (leftBotBack.x + rightTopFront.x)/2,
                (leftBotBack.y + rightTopFront.y)/2,
                (leftBotBack.z + rightTopFront.z)/2,
                1
        );

        return center;
    }

    protected Vector4f setcenter(Vector4f v1, Vector4f v2) {
        Vector4f center = new Vector4f(((v1.x+v2.x)/2), ((v1.y+v2.y)/2), ((v1.z+v2.z)/2), ((v1.w+v2.w)/2));
        return center;
    }

    public INode explodeNode(Vector4f pc) {
        Vector4f v = new Vector4f(center.x -pc.x, center.y-pc.y, center.z-pc.z, 1).normalize();
        INode translate = new TransformNode(scenegraph, "exploded: "+ name);
        translate.setTransform(new Matrix4f().translate(10*v.x,10*v.y,10*v.z));
        translate.addChild(this);

        return translate;
    }

    public IScenegraph expload(IScenegraph sg, IScenegraph blank)
    {
        INode node =new TransformNode(blank,"Exploaded "+ name);
        node.setTransform(new Matrix4f().translate(-10,0,0));
        node.addChild(this.clone());
        blank.addNode("Exploaded"+name, node);
        blank.addPolygonMesh(objInstanceName,meshes.get(objInstanceName));
        return blank;
    }

    public int rayCast(Ray r1,Stack<Matrix4f> modelview, Map<String, TextureImage> tex, ArrayList<Light> ls) //object -> view
    {

        TextureImage t = tex.get(textureName);
        Matrix4f inv = (modelview.peek().invert());
        inv.transform(r1.s);
        inv.transform(r1.v);

        //r1.s = r1.s.mul(inv);
        //r1.v = r1.v.mul(inv);
        if(this.objInstanceName.equals("Box")) {
            float tmin = (float) (-0.5 - r1.s.x) / r1.v.x;
            float tmax = (float) (0.5 - r1.s.x) / r1.v.x;

            if (tmin > tmax)
            {
                float  temp = tmax;
                tmax = tmin;
                tmin = temp;
            }

            float tymin = (float) (-0.5 - r1.s.y) / r1.v.y;
            float tymax = (float) (0.5 - r1.s.y) / r1.v.y;

            if (tymin > tymax)
            {
                float temp = tymax;
                tymax = tymin;
                tymin = temp;
            }

            if ((tmin > tymax) || (tymin > tmax))
                return new Color().toInt();

            if (tymin > tmin)
                tmin = tymin;

            if (tymax < tmax)
                tmax = tymax;

            float tzmin = (float) (-0.5 - r1.s.z) / r1.v.z;
            float tzmax = (float) (0.5 - r1.s.z) / r1.v.z;

            if (tzmin > tzmax)
            {
                float temp = tzmax;
                tzmax = tzmin;
                tzmin = temp;
            }

            if ((tmin > tzmax) || (tzmin > tmax))
                return new  Color().toInt();

            if (tzmin > tmin)
                tmin = tzmin;

            if (tzmax < tmax)
                tmax = tzmax;

            if(tmin<0 && tmax<0)
                return new  Color().toInt();

            if(tmin==0 && tmax==0)
                return new  Color().toInt();

            Vector4f p1, p2;
            p1 = new Vector4f(0,0,0,1);
            p1.x = r1.s.x + (r1.v.x * tmin);
            p1.y = r1.s.y + (r1.v.y * tmin);
            p1.z = r1.s.z + (r1.v.z * tmin);

            p2 = new Vector4f(0,0,0,1);
            p2.x = r1.s.x + (r1.v.x * tmax);
            p2.y = r1.s.y + (r1.v.y * tmax);
            p2.z = r1.s.z + (r1.v.z * tmax);

            float d1,d2;
            d1 = (float)Math.sqrt((Math.pow((p1.x-r1.s.x),2))+Math.pow((p1.y-r1.s.y),2)+Math.pow((p1.z-r1.s.z),2));
            d2 = (float)Math.sqrt((Math.pow((p2.x-r1.s.x),2))+Math.pow((p2.y-r1.s.y),2)+Math.pow((p2.z-r1.s.z),2));

            if(d1<d2)
            {
                return shade(p1,t, ls,modelview.peek()).toInt();}
            else
            {
                return shade(p2, t, ls,modelview.peek()).toInt();}
        }
        else
        {
            float tmax, tmin;
            float A,B,C;





            A = ((float)(Math.pow((double)(r1.v.x), 2)) + (float)(Math.pow((double)(r1.v.y), 2))  + (float)(Math.pow((double)(r1.v.z), 2)) );
            B = ((2*(r1.v.x*r1.s.x))+ (2*(r1.v.y*r1.s.y))+(2*(r1.v.z*r1.s.z)));
            C = (((float)(Math.pow((double)(r1.s.x), 2)) + (float)(Math.pow((double)(r1.s.y), 2)) + (float)(Math.pow((double)(r1.s.z), 2))) - 1);

            if((float)((Math.pow((double) B, 2))) < (4*(A*C))) {
                return new  Color().toInt();
            }

            if((float)((Math.pow((double) B, 2))) == (4*(A*C))) {
                tmin = (- B - ((float)Math.sqrt((Math.pow((double)(B), 2))-(4*(A*C)))))/(2*A);
                Vector4f p1;
                p1 = new Vector4f(0,0,0,1);
                p1.x = r1.s.x + (r1.v.x * tmin);
                p1.y = r1.s.y + (r1.v.y * tmin);
                p1.z = r1.s.z + (r1.v.z * tmin);
                return shade(p1, t, ls,modelview.peek()).toInt();
            }
            tmin = (- B - ((float)Math.sqrt((Math.pow((double)(B), 2))-(4*(A*C)))))/(2*A);
            tmax = (- B + ((float)Math.sqrt((Math.pow((double)(B), 2))-(4*(A*C)))))/(2*A);

            if(tmin> tmax) {
                float temp = tmin;
                tmin = tmax;
                tmax = temp;
            }

            if(tmin <0) {
                tmin = tmax;
                if(tmin<0) {
                    return new  Color().toInt();
                }
            }
            // Points of intersection
            Vector4f p1, p2;
            p1 = new Vector4f(0,0,0,1);
            p1.x = r1.s.x + (r1.v.x * tmin);
            p1.y = r1.s.y + (r1.v.y * tmin);
            p1.z = r1.s.z + (r1.v.z * tmin);

            p2 = new Vector4f(0,0,0,1);
            p2.x = r1.s.x + (r1.v.x * tmax);
            p2.y = r1.s.y + (r1.v.y * tmax);
            p2.z = r1.s.z + (r1.v.z * tmax);

            float d1,d2;
            d1 = (float)Math.sqrt((Math.pow((p1.x-r1.s.x),2))+Math.pow((p1.y-r1.s.y),2)+Math.pow((p1.z-r1.s.z),2));
            d2 = (float)Math.sqrt((Math.pow((p2.x-r1.s.x),2))+Math.pow((p2.y-r1.s.y),2)+Math.pow((p2.z-r1.s.z),2));

            if(d1<d2)
            {
                return shade(p1, t, ls,modelview.peek()).toInt();

            }
            else {
                return shade(p2, t, ls, modelview.peek()).toInt();
            }
        }
    }

    private Color shade(Vector4f p1, TextureImage tex, ArrayList<Light> ls, Matrix4f modelView)
    {
        Vector3f lv;
        Vector3f amb = new Vector3f();
        Vector3f dif = new Vector3f();
        Vector3f spec = new Vector3f();
        Matrix3f normalmatrix = new Matrix3f(modelView);
        normalmatrix = normalmatrix.invert().transpose();
        Vector3f viewVec;
        Vector3f reflectVec;
        Vector4f norm;
        Vector3f normalView;
        Vector4f texcolor;
        //texture coords
        float s,t;
        float phi, theta;
        Color c= new Color();
        if(objInstanceName.equals("Box"))
            norm = getNormalBox(p1);
        else
            norm = getNormalSphere(p1);
        float nDotl, rDotv;

        if(objInstanceName.equals("Box")) {
            s = 0;
            t = 0;
            texcolor = tex.getColor(s,t);
        } else {
            phi = (float)Math.asin(p1.y);
            theta = (float)Math.atan2(p1.z, p1.x);
            s = (float) ((theta + Math.PI)/(2*Math.PI));
            t = (float) ((phi + (Math.PI/2)/Math.PI));
            texcolor = tex.getColor(s,t);
        }


        //fposition.mul(modelView.invert());
        //norm.mul(modelView.invert());

        normalView = new Vector3f(norm.x, norm.y, norm.z).normalize();

        ls.addAll(lights);
        //frag shader
        for (int i = 0; i < ls.size(); i++) {
            Light l = ls.get(i);
            if (l.getPosition().w != 0) {
                lv = new Vector3f(ls.get(i).getPosition().x - p1.x,
                                  (ls.get(i).getPosition().y - p1.y),
                                  (ls.get(i).getPosition().z - p1.z)).normalize();
            }
            else {
                lv = new Vector3f(-ls.get(i).getPosition().x,
                                  -ls.get(i).getPosition().y,
                                  -ls.get(i).getPosition().z).normalize();
            }
            nDotl = lv.dot(normalView);

            viewVec = new Vector3f(-p1.x,
                    -p1.y,
                    -p1.z).normalize();
            reflectVec = lv.negate().reflect(normalView).normalize();
            rDotv = Math.max((viewVec.dot(reflectVec)),0.0f);
            //System.out.println(rDotv);
            amb =  new Vector3f(amb.x + (l.getAmbient().x * material.getAmbient().x),
                                amb.y + (l.getAmbient().y * material.getAmbient().y),
                                amb.z + (l.getAmbient().z * material.getAmbient().z));
            c.addColor(amb.x,amb.y,amb.z);

            dif = new Vector3f( (l.getDiffuse().x * material.getDiffuse().x * Math.max(nDotl,0)),
                                (l.getDiffuse().y * material.getDiffuse().y * Math.max(nDotl,0)),
                                (l.getDiffuse().z * material.getDiffuse().z * Math.max(nDotl,0)));
            c.addColor(dif.x,dif.y,dif.z);

            if(nDotl>0)
            {
                spec = new Vector3f( (l.getSpecular().x * material.getSpecular().x * (float)Math.pow(rDotv,material.getShininess())),
                                     (l.getSpecular().y * material.getSpecular().y * (float)Math.pow(rDotv,material.getShininess())),
                                     (l.getSpecular().z * material.getSpecular().z * (float)Math.pow(rDotv,material.getShininess())));
                //c.addColor(100,100,100);
                c.addColor(spec.x,spec.y,spec.z);
            }
            else
               c.addColor(10,0,0);
            //c.addColor(spec.x,spec.y,spec.z);
        }
        c.addTextureColor(texcolor.x, texcolor.y, texcolor.z);
        return c;
    }

    private Vector4f getNormalBox(Vector4f p1)
    {
//           if(p1.x > 0.4 && p1.x < 0.6)
//               return new Vector3f(1,0,0);
//           else if(p1.x < -0.4 && p1.x > -0.6)
//               return new Vector3f(-1,0,0);
//           else if(p1.z > 0.4 && p1.z < 0.6)
//               return new Vector3f(0,0,1);
//           else if(p1.z < -0.4 && p1.z > -0.6)
//               return new Vector3f(0,0,-1);
//           else if(p1.y > 0.4 && p1.y < 0.6)
//               return new Vector3f(0,1,0);
//           else if(p1.y < -0.4 && p1.y > -0.6)
//               return new Vector3f(0,-1,0);
//           else if(p1.z > 0.4 && p1.z < 0.6 && p1.x > 0.4 && p1.x < 0.6)
//               return new Vector3f(1,0,1);
//           else if(p1.z > 0.4 && p1.z < 0.6 && p1.x < -0.4 && p1.x > -0.6)
//               return new Vector3f(-1,0, 1);
//           else if(p1.z > 0.4 && p1.z < 0.6 && p1.y < -0.4 && p1.y > -0.6)
//               return new Vector3f(0,-1, 1);
//           else if(p1.z > 0.4 && p1.z < 0.6 && p1.y > 0.4 && p1.y < 0.6)
//               return new Vector3f(0,1, 1);
        if(p1.z == 0.5)
        return new Vector4f(0,0,-1, 0);
        else
        {
            System.out.println("Error");
            return new Vector4f();
        }
    }

    private Vector4f getNormalSphere(Vector4f p1)
    {
        return new Vector4f(p1.x,p1.y,p1.z,p1.w);
    }

//    private void getBoxTexture(Vector4f p1, TextureImage tex)
//    {
//        if(p1.x == 0.5)
//        {
//            float t = (float)(0.25*(p1.y+0.5)+0.25);
//            float s = (float)(0.25*(p1.z+0.5)+0.5);
//        }
//        else if(p1.x == -0.5)
//        {
//            float t = (float)(0.25*(p1.y+0.5)+0.25);
//            float s = (float)(0.25*(p1.z+0.5)+0.5);
//        }
//        else if(p1.z == 0.5)
//            return new Vector3f(0,0,1);
//        else if(p1.z == -0.5)
//            return new Vector3f(0,0,-1);
//        else if(p1.y == 0.5)
//            return new Vector3f(0,1,0);
//        else
//            return new Vector3f(0,-1,0);
//
//    }
    private Vector4f getSphereTexture(Vector4f p1, TextureImage tex)
    {
        float phi = (float)Math.asin(p1.y);
        float t =  (float)((phi+ (Math.PI/2))/Math.PI);
        float theta = (float)Math.atan2(p1.z,p1.x);
        float s = (float)((theta+Math.PI)/2*Math.PI);
        float tex_x = tex.getTexture().getWidth() * s;
        float tex_y = tex.getTexture().getHeight() * t;
        return new Vector4f(tex_x,tex_y,0,1);
    }
}
