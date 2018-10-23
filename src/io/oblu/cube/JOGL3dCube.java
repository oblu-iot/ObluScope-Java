/*
 * ©2015–2018, David J. Eck.

 * This work is licensed under a Creative Commons Attribution-Noncommercial-ShareAlike 4.0 License. 
 *   https://creativecommons.org/licenses/by-nc-sa/4.0/
 * 
 * (This license allows you to redistribute this book in unmodified form for non-commercial purposes. 
 * It allows you to make and distribute modified versions for non-commercial purposes, as long as you
 * include an attribution to the original author, clearly describe the modifications that you have made,
 * and distribute the modified work under the same license as the original. Permission might be given by
 * the author for other uses. See the license for full details.)
 */

/*
* * Copyright (C) 2018 GT Silicon Pvt Ltd
 *
 * Licensed under the Creative Commons Attribution 4.0
 * International Public License (the "CCBY4.0 License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://creativecommons.org/licenses/by/4.0/legalcode
 *
 * Note that the CCBY4.0 license is applicable only for the modifications
   made
 * by GT Silicon Pvt Ltd
 *
 * Modifications made by GT Silicon Pvt Ltd are within the following
comments:
 * // BEGIN - Added by GT Silicon - BEGIN //
 * {Code included or modified by GT Silicon}
 * // END - Added by GT Silicon - END //
*
* */


package io.oblu.cube;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import io.oblu.commn.Constants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;

/**
 *
 * @author gts-pc-1
 */
public class JOGL3dCube extends GLJPanel implements GLEventListener, KeyListener{
    
    
    private Camera camera;  // Contains the current view transform and projection.
                             // The user can drag with the mouse to rotate the view.

    private int currentObject;  // Code for the object  displayed in the panel.

    private int currentTexture;  // Code for the texture currently in use.
    
    private String[] textureFileNames = {
            "oblu.bmp"
//            "Earth-1024x512.jpg",
//            "NightEarth-512x256.jpg",
//            "brick001.jpg",
//            "marble.jpg",
//            "metal003.gif",
//            "mandelbrot.jpeg",
//            "wood.bmp"
            
    };
    
    private Texture[] textures = new Texture[textureFileNames.length];
    
    private final static int
         SPHERE = 0,
         CYLINDER = 1,
         CONE = 2,
         CUBE = 3,
         TORUS = 4,
         TEAPOT = 5,
         SQUARE = 6,
         CIRCLE = 7,
         RING = 8;
    
    
    private GLUT glut = new GLUT();


    /**
     * Create a TextureDemo panel to show an object with a texture, and sets up
     * the camera and trackball so that the object can be rotated with the mouse.
     */
    public JOGL3dCube() {
        setPreferredSize(new Dimension(600,600));
        addGLEventListener(this);
        camera = new Camera();
        camera.setScale(1);
        camera.installTrackball(this);
        addKeyListener(this);
//        currentTexture = 2;
        
        for (int i = 0; i < textureFileNames.length; i++) {
            if (textureFileNames[i].equals(Constants.texture))
                    {
                        currentTexture = i;
                    }
            else
            {
                currentTexture = 0;
            }
        }
        // BEGIN - Added by GT Silicon - BEGIN //
        switch (Constants.SHAPE.toUpperCase()) {
            case "SPHERE":
                currentObject = SPHERE;
                break;
            case "CYLINDER":
                currentObject = CYLINDER;
                break;
            case "CONE":
                currentObject = CONE;
                break;
            case "CUBE":
                currentObject = CUBE;
                break;
            case "TORUS":
                currentObject = TORUS;
                break;
            case "TEAPOT":
                currentObject = TEAPOT;
                break;
            case "SQUARE":
                currentObject = SQUARE;
                break;
            case "CIRCLE":
                currentObject = CIRCLE;
                break;
            case "RING":
                currentObject = RING;
                break;
            default:
                break;
        }
        // END - Added by GT Silicon - END //
    }

    /**
     * This method will be called when the GLJPanel is first
     * created.  Here, in addition to the usual setup for 3D,
     * it sets a white material color, suitable for texturing.
     * It also loads, binds and enables the texture. (Since the
     * one texture will be used throughout the program, it
     * makes sense to do this once in the init method.)
     * @param drawable
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0,0,0,1);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] { 1,1,1,1 }, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] { 0.3f, 0.3f, 0.3f, 1 }, 0);
        gl.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 100);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
             // I am cheating by using separate specular color, which requires OpenGL 1.2, but
             // it gives nicer specular highlights on textured surfaces.
        for (int i = 0; i < textureFileNames.length; i++) {
            try 
            {
                File textureURL = new File(".//images/" + textureFileNames[i]);
//                System.out.println(textureURL.getName());
                if (textureURL != null) {
                    BufferedImage img = ImageIO.read(textureURL);
                    ImageUtil.flipImageVertically(img);
                    textures[i] = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);
                    textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
                    textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
                    if (textureURL.getName().equals(Constants.texture))
                    {
                        textures[i].enable(gl);
                    }
                    else
                    {
                        textures[0].enable(gl);
                    }
                }
              
            }
            catch (GLException | IOException e) {
                e.printStackTrace();
            }
        }
//        textures[0].enable(gl);
    }
    
     // BEGIN - Added by GT Silicon - BEGIN //
    
    DecimalFormat  df3 = new DecimalFormat("0.000");
    float[] quaternionData = new float[4];
    float[] data = new float[16];
    TextRenderer textRenderer = new TextRenderer(new Font("Verdana", Font.BOLD, 12));
    
    public void setQuaterniunData(float[] quaternionData) {
        this.quaternionData = quaternionData;
        repaint();
    }
    
    // END - Added by GT Silicon - END //
    
    /**
     * Display method renders the current object, using the current texture.
     * @param drawable
     */
    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        camera.apply(gl);
        if (currentObject <= CONE)
            gl.glRotated(90,-1,0,0);  // rotate AXIS of object from z-AXIS to y-AXIS
        if (currentObject == CONE || currentObject == CYLINDER)
            gl.glTranslated(0,0,-0.5);  // moves center of object to the origin
        
        // BEGIN - Added by GT Silicon - BEGIN //
        Quaternion quaternion = new Quaternion(quaternionData[1], quaternionData[3], quaternionData[2]*-1, quaternionData[0]);
    
        gl.glMultMatrixf(quaternion.toMatrix(data, 0),0);
            
        textRenderer.beginRendering(500, 500);
        textRenderer.setColor(Color.YELLOW);
        textRenderer.setSmoothing(true);

        Point pt = new Point(10, 10);
        textRenderer.draw("Quaternion", 10, 90);
        textRenderer.draw("Z: "+ df3.format(quaternionData[3]), (int) (pt.x), (int) (pt.y));
        textRenderer.draw("Y: "+ df3.format(quaternionData[2]), 10, 30);
        textRenderer.draw("X: "+ df3.format(quaternionData[1]), 10, 50);
        textRenderer.draw("W: "+ df3.format(quaternionData[0]), 10, 70);
        textRenderer.endRendering();
        // END - Added by GT Silicon - END //

        textures[currentTexture].bind(gl);  // Says which texture to use.

        switch (currentObject) {
        case SPHERE: TexturedShapes.uvSphere(gl); break;
        case CYLINDER: TexturedShapes.uvCylinder(gl); break;
        case CONE: TexturedShapes.uvCone(gl); break;
        case CUBE: TexturedShapes.cube(gl); break;
        case TORUS: TexturedShapes.uvTorus(gl); break;
        case TEAPOT: 
            gl.glFrontFace(GL2.GL_CW); // Teapot has non-standard front faces
                                       // that don't work right with two-sided lighting.
                                       // This reverses the usual test for front face.
            glut.glutSolidTeapot(0.5);
            gl.glFrontFace(GL2.GL_CCW);  
            break;
        case SQUARE: TexturedShapes.square(gl); break;
        case CIRCLE: TexturedShapes.circle(gl); break;
        case RING: TexturedShapes.ring(gl); break;
        }
    }

    // Extra, unused methods of the GLEventListener interface
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) { }

    /**
     *
     * @param drawable
     */
    @Override
    public void dispose(GLAutoDrawable drawable) { }

    double rotateX = 0;    // rotations of the cube about the axes
    double rotateY = 0;
    double rotateZ = 0;
    
    public void setRotateXYZ(double rotateX, double rotateY, double rotateZ){
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
        repaint();
    }
    
    /**
     * Left/right arrow keys change the current object.
     * Up/down arrow keys change the current texture.
     * Home key restores the default camera viewpoint.
     * @param e
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                currentTexture++;
                if (currentTexture >= textureFileNames.length)
                    currentTexture = 0;
                break;
            case KeyEvent.VK_DOWN:
                currentTexture--;
                if (currentTexture < 0 )
                    currentTexture = textureFileNames.length - 1;
                break;
            case KeyEvent.VK_LEFT:
                currentObject--;
                if (currentObject < 0)
                    currentObject = 8;
                break;
            case KeyEvent.VK_RIGHT:
                currentObject++;
                if (currentObject > 8)
                    currentObject = 0;
                break;
            case KeyEvent.VK_HOME:
                camera.lookAt(0,0,30, 0,0,0, 0,1,0);
                break;
            default:
                break;
        }
        repaint();
    }

    // Extra, unused methods of the KeyListener interface.
    @Override
    public void keyReleased(KeyEvent e) { }
    @Override
    public void keyTyped(KeyEvent e) { }
    
    
}