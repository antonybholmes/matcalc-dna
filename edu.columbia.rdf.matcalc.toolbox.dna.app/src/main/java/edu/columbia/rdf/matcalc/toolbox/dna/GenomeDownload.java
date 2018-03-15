package edu.columbia.rdf.matcalc.toolbox.dna;

import java.net.URL;

import org.jebtk.core.NameProperty;

public class GenomeDownload implements NameProperty {
  private final URL mChrs;
  private final URL mDna;
  private final String mName;

  public GenomeDownload(String name, URL chrs, URL dna) {
    mName = name;
    mChrs = chrs;
    mDna = dna;
  }

  @Override
  public String getName() {
    return mName;
  }

  public URL getChrURL() {
    return mChrs;
  }

  public URL getDNAURL() {
    return mDna;
  }
}
