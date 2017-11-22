package sgraph;

import com.jogamp.opengl.GL3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
        thismodelView = new Matrix4f();
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

    public int rayCast(Ray r1,Stack<Matrix4f> modelview) //object -> view
    {
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
                return 0;

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
                return 0;

            if (tzmin > tmin)
                tmin = tzmin;

            if (tzmax < tmax)
                tmax = tzmax;

            if(tmin<0 && tmax<0)
                return 0;

            if(tmin==0 && tmax==0)
                return 0;


//            Vector4f p1, p2;
//            p1 = new Vector4f(0,0,0,1);
//            p1.x = r1.s.x + (r1.v.x * tmin);
//            p1.y = r1.s.y + (r1.v.y * tmin);
//            p1.z = r1.s.z + (r1.v.z * tmin);
//
//            p2 = new Vector4f(0,0,0,1);
//            p2.x = r1.s.x + (r1.v.x * tmax);
//            p2.y = r1.s.y + (r1.v.y * tmax);
//            p2.z = r1.s.z + (r1.v.z * tmax);
            return 1;
        }
        else
        {
            float tmax, tmin;
            float A,B,C;


            A = ((float)(Math.pow((double)(r1.v.x), 2)) + (float)(Math.pow((double)(r1.v.y), 2))  + (float)(Math.pow((double)(r1.v.z), 2)) );
            B = ((2*(r1.v.x*r1.s.x))+ (2*(r1.v.y*r1.s.y))+(2*(r1.v.z*r1.s.z)));
            C = (((float)(Math.pow((double)(r1.s.x), 2)) + (float)(Math.pow((double)(r1.s.y), 2)) + (float)(Math.pow((double)(r1.s.z), 2))) - 1);

            tmin = - B - ((float)Math.sqrt((Math.pow((double)(B), 2))-(4*(A*C))));
            tmax = - B + ((float)Math.sqrt((Math.pow((double)(B), 2))-(4*(A*C))));

            if((float)((Math.pow((double) B, 2))) < (4*(A*C))) {
                return 0;
            }

            if(tmin> tmax) {
                float temp = tmin;
                tmin = tmax;
                tmax = temp;
            }

            if(tmin <0) {
                tmin = tmax;
            }

            if(tmin<0) {
                return 0;
            }

            return 1;

//             Vector4f p1= new Vector4f(-r1.v.x*(2/800 +1),-r1.v.y*(2/800 +1),0,1);
//             float dx,dy,dz;
//
//             dx = p1.x - r1.v.x;
//             dy = p1.y - r1.v.y;
//             dz = p1.z - r1.v.z;
//
//             A = (dx*dx)+(dy*dy)+(dz*dz);
//             B = 2*dx*(r1.v.x)+2*dy*(r1.v.y)+2*dz*(r1.v.z);
//             C = r1.v.x*r1.v.x+r1.v.y*r1.v.y+r1.v.z*r1.v.z-(float)10*10;
//
//            if(B*B-4*A*C < 0)
//                return 0;
//            if(B*B == (4*A*C))
//            {
//                tmin = (float)(-B/2*A);
//                return 1;
//            }
//            tmin = (float)((-B-(Math.sqrt(B*B - 4*A*C)))/2*A);
//            tmax = (float)((-B+(Math.sqrt(B*B - 4*A*C)))/2*A);
//            return 1;


        }
    }

    public void swap(float x, float y)
    {
        float temp;
        temp = y;
        y = x;
        x = temp;
    }
}
