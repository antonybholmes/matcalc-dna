package edu.columbia.rdf.matcalc.toolbox.dna.app;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.UIService;
import org.jebtk.modern.help.GuiAppInfo;

public class DnaInfo extends GuiAppInfo {

  public DnaInfo() {
    super("DNA", new AppVersion(3), "Copyright (C) 2014-2016 Antony Holmes",
        UIService.getInstance().loadIcon(DnaIcon.class, 32), UIService.getInstance().loadIcon(DnaIcon.class, 128),
        "Extract DNA sequences.");
  }

}
