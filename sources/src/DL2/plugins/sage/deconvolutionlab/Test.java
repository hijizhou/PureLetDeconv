package DL2.plugins.sage.deconvolutionlab;

import DL2.bilib.tools.Files;
import DL2.deconvolutionlab.Imager;
import DL2.deconvolutionlab.Lab;
import DL2.deconvolutionlab.dialog.OutputDialog;
import DL2.deconvolutionlab.output.Output.View;
import icy.gui.frame.IcyFrame;
import icy.plugin.abstract_.PluginActionable;

public class Test extends PluginActionable {

	@Override
	public void run() {
		
		Lab.init(Imager.Platform.ICY, Files.getWorkingDirectory() + "DeconvolutionLab2.config");
		IcyFrame icf = new IcyFrame();
		icf.add(new OutputDialog(View.FIGURE).getContentPane());	
		icf.pack();
		icf.toFront();
		icf.addToDesktopPane();
		icf.setVisible(true);
		//icf.setResizable(true);

		
	}

}
