package linoleum.j3d;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.vp.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.swing.JPanel;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import linoleum.application.Frame;

public class ObjLoad extends Frame {

    private final boolean spin = false;
    private final boolean noTriangulate = false;
    private final boolean noStripify = false;
    private final double creaseAngle = 60.0;
    private final String title = "3D Object Loader";

    public BranchGroup createSceneGraph(final URL filename) throws Exception {
	// Create the root of the branch graph
	final BranchGroup objRoot = new BranchGroup();

        // Create a Transformgroup to scale all objects so they
        // appear in the scene.
        final TransformGroup objScale = new TransformGroup();
        final Transform3D t3d = new Transform3D();
        t3d.setScale(0.7);
        objScale.setTransform(t3d);
        objRoot.addChild(objScale);

	// Create the transform group node and initialize it to the
	// identity.  Enable the TRANSFORM_WRITE capability so that
	// our behavior code can modify it at runtime.  Add it to the
	// root of the subgraph.
	final TransformGroup objTrans = new TransformGroup();
	objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	objScale.addChild(objTrans);

	int flags = ObjectFile.RESIZE;
	if (!noTriangulate) flags |= ObjectFile.TRIANGULATE;
	if (!noStripify) flags |= ObjectFile.STRIPIFY;
	final ObjectFile f = new ObjectFile(flags, (float)(creaseAngle * Math.PI / 180.0));
	final Scene s = f.load(filename);

	objTrans.addChild(s.getSceneGroup());

	final BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        if (spin) {
	  final Transform3D yAxis = new Transform3D();
	  final Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE,
						0, 0,
						4000, 0, 0,
						0, 0, 0);

	  final RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objTrans, yAxis,
									0.0f, (float) Math.PI*2.0f);
	  rotator.setSchedulingBounds(bounds);
	  objTrans.addChild(rotator);
	}

        // Set up the background
        final Color3f bgColor = new Color3f(0.05f, 0.05f, 0.5f);
        final Background bgNode = new Background(bgColor);
        bgNode.setApplicationBounds(bounds);
        objRoot.addChild(bgNode);

	return objRoot;
    }

    private SimpleUniverse createUniverse(final Canvas3D canvas3d) {
	// Create simple universe with view branch
	final SimpleUniverse univ = new SimpleUniverse(canvas3d);
        final BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

	// add mouse behaviors to the ViewingPlatform
	final ViewingPlatform viewingPlatform = univ.getViewingPlatform();

	final PlatformGeometry pg = new PlatformGeometry();

	// Set up the ambient light
	final Color3f ambientColor = new Color3f(0.1f, 0.1f, 0.1f);
	final AmbientLight ambientLightNode = new AmbientLight(ambientColor);
	ambientLightNode.setInfluencingBounds(bounds);
	pg.addChild(ambientLightNode);

	// Set up the directional lights
	final Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
	final Vector3f light1Direction  = new Vector3f(1.0f, 1.0f, 1.0f);
	final Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
	final Vector3f light2Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);

	final DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
	light1.setInfluencingBounds(bounds);
	pg.addChild(light1);

	final DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
	light2.setInfluencingBounds(bounds);
	pg.addChild(light2);

	viewingPlatform.setPlatformGeometry( pg );

	// This will move the ViewPlatform back a bit so the
	// objects in the scene can be viewed.
	viewingPlatform.setNominalViewingTransform();

	if (!spin) {
            final OrbitBehavior orbit = new OrbitBehavior(canvas3d, OrbitBehavior.REVERSE_ALL);
            orbit.setSchedulingBounds(bounds);
            viewingPlatform.setViewPlatformBehavior(orbit);	    
	}        

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
	univ.getViewer().getView().setMinimumFrameCycleTime(5);

	return univ;
    }

	public ObjLoad() {
		initComponents();
		setMimeType("application/wavefront-obj");
		setTitle(title);
	}

	@Override
	protected void open() {
		final URI uri = getURI();
		drawingPanel.removeAll();
		setTitle(title);
		if (uri != null) try {
			final URL filename = uri.toURL();
			setTitle(new File(filename.getPath()).getName());

			// Get the preferred graphics configuration for the default screen
			final GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

			// Create a Canvas3D using the preferred configuration
			final Canvas3D c = new Canvas3D(config);
			new JPanel().add(c);

			// Create SimpleUniverse; add canvas to drawing panel
			final SimpleUniverse univ = createUniverse(c);
			drawingPanel.add(c, BorderLayout.CENTER);

			// Create the content branch and add it to the universe
			final BranchGroup scene = createSceneGraph(filename);
			univ.addBranchGraph(scene);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void close() {
		setURI(null);
	}

        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                drawingPanel = new javax.swing.JPanel();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);

                drawingPanel.setPreferredSize(new java.awt.Dimension(394, 296));
                drawingPanel.setLayout(new java.awt.BorderLayout());
                getContentPane().add(drawingPanel, java.awt.BorderLayout.CENTER);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel drawingPanel;
        // End of variables declaration//GEN-END:variables

}
