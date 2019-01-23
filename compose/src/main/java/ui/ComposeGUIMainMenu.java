package ui;
import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;

import org.jfree.ui.RefineryUtilities;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.OntologyOperations;
import equivalencematching.EditMatcher_remove;
import equivalencematching.ISubMatcher;
import equivalencematching.SmoaMatcher_remove;
import equivalencematching.TrigramMatcher;
import equivalencematching.WNRiWordNetDistance;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import graph.Graph;
import matchercombination.ParallelCombination;
import matchercombination.SequentialCombination;
import net.didion.jwnl.JWNLException;
import ontologyprofiling.OntologyProfiler;
import subsumptionmatching.AncestorMatcher;
import subsumptionmatching.CompoundMatcher;
import subsumptionmatching.ParentMatcher;
import subsumptionmatching.WNHyponymMatcher;
import utilities.AlignmentOperations;
import utilities.StringUtilities;
import utilities.WNDomain;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import javax.swing.JSlider;

/**
 * @author audunvennesland
 * 16. aug. 2017 
 */
public class ComposeGUIMainMenu extends JFrame {

	private JFrame frame;
	private JPanel panelMenu;
	private JPanel panelSubsumptionMatching;
	private JPanel panelEquivalenceMatching;
	StringBuilder sbOntologyProfile1 = new StringBuilder();
	StringBuilder sbOntologyProfile2 = new StringBuilder();
	StringBuilder sbMatchingResults = new StringBuilder();

	private File ontoFile1 = null;
	private File ontoFile2 = null;
	private File refAlignFile = null;
	Properties params = null;
	AlignmentProcess a = null;
	File outputAlignment = null;
	double threshold;
	String alignmentFileName = null;
	PrintWriter writer = null;
	AlignmentVisitor renderer = null;
	AlignmentParser aparser = new AlignmentParser(0);

	OWLOntologyManager manager;
	OWLOntology o1;
	OWLOntology o2;
	Label labelO1;
	Label labelO2;
	Graph creator;
	private JTextField textFieldCompoundMatcherPriority;
	private JTextField textFieldParentMatcherPriority;
	private JTextField textFieldAncestorMatcherPriority;
	private JTextField textFieldWordNetMatcherPriority;

	String compoundAlignmentFileName = null;
	String parentMatcherAlignmentFileName = null;
	String ancestorMatcherAlignmentFileName = null;
	String wordNetMatcherAlignmentFileName = null;
	
	String editAlignmentFileName = null;
	String smoaAlignmentFileName = null;
	String iSubAlignmentFileName = null;
	String trigramAlignmentFileName = null;
	String wordNetAlignmentFileName = null;
	String structureAlignmentFileName = null;



	//	/*** USED FOR INCLUDING THE ONTOLOGY FILE NAMES IN THE COMPUTED ALIGNMENT FILE ***/
	//	String onto1 = StringUtils.stripPath(ontoFile1.toString());
	//	String onto2 = StringUtils.stripPath(ontoFile2.toString());

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException, OWLOntologyCreationException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ComposeGUIMainMenu window = new ComposeGUIMainMenu();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ComposeGUIMainMenu() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));

		//panels
		final JPanel panelMenu = new JPanel();
		frame.getContentPane().add(panelMenu, "name_25757788791192");
		panelMenu.setLayout(null);
		panelMenu.setVisible(true);


		/*** Equivalence matching ***/
		final JPanel panelEquivalenceMatching = new JPanel();
		panelEquivalenceMatching.setBackground(Color.WHITE);
		frame.getContentPane().add(panelEquivalenceMatching, "name_25766809915035");
		panelEquivalenceMatching.setLayout(null);
		
		final JPanel EQgraphJPanel = new JPanel();
		EQgraphJPanel.setBorder(null);
		EQgraphJPanel.setBackground(Color.WHITE);
		EQgraphJPanel.setBounds(456, 215, 491, 315);
		panelEquivalenceMatching.add(EQgraphJPanel);
		
		
		
		final JEditorPane EQmatchingResultsPane = new JEditorPane();
		EQmatchingResultsPane.setFont(new Font("Lucida Grande", Font.PLAIN, 8));
		EQmatchingResultsPane.setForeground(Color.WHITE);
		EQmatchingResultsPane.setBackground(Color.WHITE);
		EQmatchingResultsPane.setBounds(595, 538, 352, 114);
		panelEquivalenceMatching.add(EQmatchingResultsPane);

		JLabel lblUploadFiles_1 = new JLabel("Upload files");
		lblUploadFiles_1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblUploadFiles_1.setForeground(new Color(0, 102, 153));
		lblUploadFiles_1.setBounds(31, 36, 105, 16);
		panelEquivalenceMatching.add(lblUploadFiles_1);

		JLabel lblSelectMatchers = new JLabel("Select matchers");
		lblSelectMatchers.setForeground(new Color(0, 102, 153));
		lblSelectMatchers.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblSelectMatchers.setBounds(31, 172, 157, 16);
		panelEquivalenceMatching.add(lblSelectMatchers);

		JLabel lblChooseConfidenceThreshold = new JLabel("Choose confidence threshold");
		lblChooseConfidenceThreshold.setForeground(new Color(0, 102, 153));
		lblChooseConfidenceThreshold.setFont(new Font("Lucida Grande", Font.BOLD, 10));
		lblChooseConfidenceThreshold.setBounds(201, 172, 157, 16);
		panelEquivalenceMatching.add(lblChooseConfidenceThreshold);

		final JCheckBox checkBoxEdit = new JCheckBox("Edit ");
		checkBoxEdit.setBounds(41, 207, 128, 23);
		panelEquivalenceMatching.add(checkBoxEdit);

		final JCheckBox checkBoxSmoa = new JCheckBox("Smoa");
		checkBoxSmoa.setBounds(41, 254, 128, 23);
		panelEquivalenceMatching.add(checkBoxSmoa);

		final JCheckBox checkBoxIsub = new JCheckBox("ISub");
		checkBoxIsub.setBounds(41, 296, 128, 23);
		panelEquivalenceMatching.add(checkBoxIsub);

		final JCheckBox checkBoxTrigram = new JCheckBox("Trigram");
		checkBoxTrigram.setBounds(41, 353, 128, 23);
		panelEquivalenceMatching.add(checkBoxTrigram);

		final JCheckBox checkBoxWordnet = new JCheckBox("WordNet");
		checkBoxWordnet.setBounds(41, 407, 128, 23);
		panelEquivalenceMatching.add(checkBoxWordnet);

		final JCheckBox checkBoxStructure = new JCheckBox("Structure");
		checkBoxStructure.setBounds(41, 460, 128, 23);
		panelEquivalenceMatching.add(checkBoxStructure);
		
		final DefaultCategoryDataset EQdataset = 
				new DefaultCategoryDataset( );  
		
		final JFreeChart EQchart = ChartFactory.createBarChart("", "", "", EQdataset, PlotOrientation.VERTICAL, true, false, false);
		
		final ChartPanel EQmatchingResultsChartPanel = new ChartPanel((JFreeChart) EQchart);

		//labels
		final JLabel EQlblOntology1 = new JLabel("");
		EQlblOntology1.setEnabled(false);
		EQlblOntology1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		EQlblOntology1.setBounds(191, 62, 133, 16);
		panelEquivalenceMatching.add(EQlblOntology1);
		
		final JLabel EQlblOntology2 = new JLabel("");
		EQlblOntology2.setEnabled(false);
		EQlblOntology2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		EQlblOntology2.setBounds(191, 93, 133, 16);
		panelEquivalenceMatching.add(EQlblOntology2);
		
		final JLabel EQlblRefAlign = new JLabel("");
		EQlblRefAlign.setEnabled(false);
		EQlblRefAlign.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		EQlblRefAlign.setBounds(191, 122, 133, 16);
		panelEquivalenceMatching.add(EQlblRefAlign);

		//Upload ontology files for equivalence matching
		JButton btnEQUploadOntology1 = new JButton("Upload ontology 1");
		btnEQUploadOntology1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OpenFile of1 = new OpenFile();

				try {
					ontoFile1 = of1.getOntoFile1();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				EQlblOntology1.setText(StringUtilities.stripPath(ontoFile1.toString()));
			}
		});
		btnEQUploadOntology1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnEQUploadOntology1.setBounds(19, 58, 169, 29);
		panelEquivalenceMatching.add(btnEQUploadOntology1);

		JButton btnEQUploadOntology2 = new JButton("Upload ontology 2");
		btnEQUploadOntology2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OpenFile of2 = new OpenFile();

				try {
					ontoFile2 = of2.getOntoFile1();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				EQlblOntology2.setText(StringUtilities.stripPath(ontoFile2.toString()));
			}
		});
		btnEQUploadOntology2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnEQUploadOntology2.setBounds(19, 90, 169, 29);
		panelEquivalenceMatching.add(btnEQUploadOntology2);

		//Upload reference alignment for equivalence matching
		JButton btnUploadReferenceAlignment_1 = new JButton("Upload Reference Alignment");
		btnUploadReferenceAlignment_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OpenFile of3 = new OpenFile();

				try {
					refAlignFile = of3.getFile();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				EQlblRefAlign.setText(StringUtilities.stripPath(refAlignFile.toString()));
			}
		});
		btnUploadReferenceAlignment_1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnUploadReferenceAlignment_1.setBounds(19, 119, 169, 29);
		panelEquivalenceMatching.add(btnUploadReferenceAlignment_1);

		final JCheckBox chckbxEnforceSameDomain = new JCheckBox("Enforce same domain constraint");
		chckbxEnforceSameDomain.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		chckbxEnforceSameDomain.setBounds(41, 508, 207, 23);
		panelEquivalenceMatching.add(chckbxEnforceSameDomain);

		final JSlider sliderEQEdit = new JSlider();
		sliderEQEdit.setBounds(181, 198, 207, 41);
		sliderEQEdit.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderEQEdit.setMinorTickSpacing(2);
		sliderEQEdit.setMajorTickSpacing(10);
		sliderEQEdit.setPaintLabels(true);
		panelEquivalenceMatching.add(sliderEQEdit);

		final JSlider sliderEQSmoa = new JSlider();
		sliderEQSmoa.setPaintLabels(true);
		sliderEQSmoa.setMinorTickSpacing(2);
		sliderEQSmoa.setMajorTickSpacing(10);
		sliderEQSmoa.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderEQSmoa.setBounds(181, 243, 207, 41);
		panelEquivalenceMatching.add(sliderEQSmoa);

		final JSlider sliderEQISub = new JSlider();
		sliderEQISub.setPaintLabels(true);
		sliderEQISub.setMinorTickSpacing(2);
		sliderEQISub.setMajorTickSpacing(10);
		sliderEQISub.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderEQISub.setBounds(181, 296, 207, 41);
		panelEquivalenceMatching.add(sliderEQISub);

		final JSlider sliderEQTrigram = new JSlider();
		sliderEQTrigram.setPaintLabels(true);
		sliderEQTrigram.setMinorTickSpacing(2);
		sliderEQTrigram.setMajorTickSpacing(10);
		sliderEQTrigram.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderEQTrigram.setBounds(181, 349, 207, 41);
		panelEquivalenceMatching.add(sliderEQTrigram);

		final JSlider sliderEQWordNet = new JSlider();
		sliderEQWordNet.setPaintLabels(true);
		sliderEQWordNet.setMinorTickSpacing(2);
		sliderEQWordNet.setMajorTickSpacing(10);
		sliderEQWordNet.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderEQWordNet.setBounds(181, 402, 207, 41);
		panelEquivalenceMatching.add(sliderEQWordNet);

		final JSlider sliderEQStructure = new JSlider();
		sliderEQStructure.setPaintLabels(true);
		sliderEQStructure.setMinorTickSpacing(2);
		sliderEQStructure.setMajorTickSpacing(10);
		sliderEQStructure.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderEQStructure.setBounds(181, 455, 207, 41);
		panelEquivalenceMatching.add(sliderEQStructure);

		JButton btnEQEvaluate = new JButton("Evaluate");
		
		btnEQEvaluate.addActionListener(new ActionListener() {
				
				
				public void actionPerformed(ActionEvent e) {

					//compound matcher
					if (checkBoxEdit.isSelected()) {

						Alignment a = new EditMatcher_remove();
						threshold = (double)sliderEQEdit.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						editAlignmentFileName = "./files/GUITest/alignments/Edit.rdf";

						outputAlignment = new File(editAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment editAlignment = (BasicAlignment)(a.clone());

						try {
							editAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							editAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {
							

							//get the alignment
							File computedAlignment = new File(editAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(editAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedAlignment = null;
						try {
							evaluatedAlignment = aparser.parse(new URI("file:"+editAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						//evaluation
						double editFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double editPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double editRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";

						EQdataset.addValue( editPrecisionValue , "Edit" , precision );        
						EQdataset.addValue( editRecallValue , "Edit" , recall );        
						EQdataset.addValue( editFMeasureValue , "Edit" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("Edit:\n");
						sbMatchingResults.append("F-measure: " + editFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + editRecallValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + editRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}

					//smoa matcher
					if (checkBoxSmoa.isSelected()) {
						Alignment a = new SmoaMatcher_remove();
					threshold = (double)sliderEQSmoa.getValue()/100;
					

					try {
						a.init(ontoFile1.toURI(), ontoFile2.toURI());
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}
					params = new Properties();
					params.setProperty("", "");
					try {
						((AlignmentProcess) a).align((Alignment)null, params);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}	


					smoaAlignmentFileName = "./files/GUITest/alignments/Smoa.rdf";

					outputAlignment = new File(smoaAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment editAlignment = (BasicAlignment)(a.clone());

					try {
						editAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						editAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();

					//check domain constraint
					if (chckbxEnforceSameDomain.isSelected()) {
						

						//get the alignment
						File computedAlignment = new File(smoaAlignmentFileName);
						AlignmentParser parser = new AlignmentParser();
						BasicAlignment alignmentDomainConstraint = null;

						String concept1 = null;
						String concept2 = null;

						try {
							alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

							for (Cell c : alignmentDomainConstraint) {
								concept1 = c.getObject1AsURI().getFragment();
								concept2 = c.getObject2AsURI().getFragment();
								if (WNDomain.sameDomain(concept1, concept2)) {
									//increase by 10 percent
									c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
								} else {
									//reduce by 10 percent
									c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
								}

							}
						} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
							e1.printStackTrace();
						}


						PrintWriter writer = null;
						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(smoaAlignmentFileName)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						try {
							alignmentDomainConstraint.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

					}

					//evaluate
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {

						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+smoaAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double smoaFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double smoaPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double smoaRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					final String precision = "Precision";        
					final String recall = "Recall";        
					final String fMeasure = "F-Measure";

					EQdataset.addValue( smoaPrecisionValue , "Smoa" , precision );        
					EQdataset.addValue( smoaRecallValue , "Smoa" , recall );        
					EQdataset.addValue( smoaFMeasureValue , "Smoa" , fMeasure );  

					sbMatchingResults.append("\n");
					sbMatchingResults.append("Smoa:\n");
					sbMatchingResults.append("F-measure: " + smoaFMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + smoaPrecisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + smoaRecallValue);

					EQmatchingResultsPane.setText(sbMatchingResults.toString());


					}
					if (checkBoxIsub.isSelected()) {

						Alignment a = new ISubMatcher();
						threshold = (double)sliderEQISub.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						iSubAlignmentFileName = "./files/GUITest/alignments/ISub.rdf";

						outputAlignment = new File(iSubAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment editAlignment = (BasicAlignment)(a.clone());

						try {
							editAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							editAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {
							

							//get the alignment
							File computedAlignment = new File(iSubAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(iSubAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedAlignment = null;
						try {
							evaluatedAlignment = aparser.parse(new URI("file:"+iSubAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						//evaluation
						double iSubFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double iSubPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double iSubRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";

						EQdataset.addValue( iSubPrecisionValue , "ISub" , precision );        
						EQdataset.addValue( iSubRecallValue , "ISub" , recall );        
						EQdataset.addValue( iSubFMeasureValue , "ISub" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("ISub:\n");
						sbMatchingResults.append("F-measure: " + iSubFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + iSubPrecisionValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + iSubRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}
					if (checkBoxTrigram.isSelected()) {

						Alignment a = new TrigramMatcher();
						threshold = (double)sliderEQTrigram.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						trigramAlignmentFileName = "./files/GUITest/alignments/Trigram.rdf";

						outputAlignment = new File(trigramAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment editAlignment = (BasicAlignment)(a.clone());

						try {
							editAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							editAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {

							//get the alignment
							File computedAlignment = new File(trigramAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(trigramAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedAlignment = null;
						try {
							evaluatedAlignment = aparser.parse(new URI("file:"+trigramAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						//evaluation
						double trigramFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double trigramPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double trigramRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";


						EQdataset.addValue( trigramPrecisionValue , "Trigram" , precision );        
						EQdataset.addValue( trigramRecallValue , "Trigram" , recall );        
						EQdataset.addValue( trigramFMeasureValue , "Trigram" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("Trigram:\n");
						sbMatchingResults.append("F-measure: " + trigramFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + trigramPrecisionValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + trigramRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}

					if (checkBoxWordnet.isSelected()) {

						Alignment a = new WNRiWordNetDistance();
						threshold = (double)sliderEQWordNet.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						wordNetAlignmentFileName = "./files/GUITest/alignments/WordNetEQ.rdf";

						outputAlignment = new File(wordNetAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment editAlignment = (BasicAlignment)(a.clone());

						try {
							editAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							editAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {
							

							//get the alignment
							File computedAlignment = new File(wordNetAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(wordNetAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedAlignment = null;
						try {
							evaluatedAlignment = aparser.parse(new URI("file:"+wordNetAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						//evaluation
						double wordNetFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double wordNetPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double wordNetRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";
	

						EQdataset.addValue( wordNetPrecisionValue , "WordNet" , precision );        
						EQdataset.addValue( wordNetRecallValue , "WordNet" , recall );        
						EQdataset.addValue( wordNetFMeasureValue , "WordNet" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("WordNet:\n");
						sbMatchingResults.append("F-measure: " + wordNetFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + wordNetPrecisionValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + wordNetRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}

					/*if (checkBoxStructure.isSelected()) {

						Alignment a = new StructureEQMatcher();
						threshold = (double)sliderEQStructure.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						structureAlignmentFileName = "./files/GUITest/alignments/StructureEQ.rdf";

						outputAlignment = new File(structureAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment editAlignment = (BasicAlignment)(a.clone());

						try {
							editAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							editAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {
							

							//get the alignment
							File computedAlignment = new File(structureAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(structureAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedAlignment = null;
						try {
							evaluatedAlignment = aparser.parse(new URI("file:"+structureAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						//evaluation
						double compoundFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double compoundPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double compoundRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";
						
						final DefaultCategoryDataset EQdataset = 
								new DefaultCategoryDataset( );  

						EQdataset.addValue( compoundPrecisionValue , "Structure" , precision );        
						EQdataset.addValue( compoundRecallValue , "Structure" , recall );        
						EQdataset.addValue( compoundFMeasureValue , "Structure" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("Structure:\n");
						sbMatchingResults.append("F-measure: " + compoundFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + compoundPrecisionValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + compoundRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}*/

					//add chart
					
					EQchart.setBorderVisible(true);
					CategoryPlot EQcp = EQchart.getCategoryPlot();
					EQcp.setBackgroundPaint(Color.white);

					
					EQmatchingResultsChartPanel.setBorder(null);
					Dimension matchingResultsDimension = new Dimension();
					matchingResultsDimension.setSize(490, 315);
					//EQgraphJPanel.setBounds(456, 215, 491, 315);
					EQmatchingResultsChartPanel.setPreferredSize(matchingResultsDimension);
					EQgraphJPanel.add(EQmatchingResultsChartPanel);
					EQgraphJPanel.setVisible(true);  

				
			}
	
		});
		btnEQEvaluate.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnEQEvaluate.setBounds(369, 619, 117, 29);
		panelEquivalenceMatching.add(btnEQEvaluate);

		JButton btnEQMainMenu = new JButton("Main Menu");
		btnEQMainMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				checkBoxEdit.setSelected(false);
				checkBoxSmoa.setSelected(false);
				checkBoxIsub.setSelected(false);
				checkBoxTrigram.setSelected(false);
				checkBoxWordnet.setSelected(false);
				checkBoxStructure.setSelected(false);
				EQmatchingResultsChartPanel.repaint();
				EQgraphJPanel.repaint();
				EQdataset.clear();
				//EQchart.
				panelEquivalenceMatching.setVisible(false);
				panelMenu.setVisible(true);

			}
		});
		btnEQMainMenu.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnEQMainMenu.setBounds(482, 619, 117, 29);
		panelEquivalenceMatching.add(btnEQMainMenu);
		panelEquivalenceMatching.setVisible(false);

		JLabel lblEQEvaluationResults = new JLabel("Evaluation Results");
		lblEQEvaluationResults.setForeground(new Color(0, 102, 153));
		lblEQEvaluationResults.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblEQEvaluationResults.setBounds(618, 172, 157, 16);
		panelEquivalenceMatching.add(lblEQEvaluationResults);
		
//		//Upload ontology files for equivalence matching
//				JButton btnEQUploadOntology1 = new JButton("Upload ontology 1");
//				btnEQUploadOntology1.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						OpenFile of1 = new OpenFile();
//
//						try {
//							ontoFile1 = of1.getOntoFile1();
//						} catch (Exception ex) {
//							ex.printStackTrace();
//						}
//
//						EQlblOntology1.setText(StringUtils.stripPath(ontoFile1.toString()));
//					}
//				});
		
		JButton btnCompute = new JButton("Compute alignment");
		
		//set to hold all alignments
		final Set<Alignment> equivalenceAlignmentSet = new HashSet<Alignment>();
		
		btnCompute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


					//compound matcher
					if (checkBoxEdit.isSelected()) {

						Alignment a = new EditMatcher_remove();
						threshold = (double)sliderEQEdit.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						editAlignmentFileName = "./files/GUITest/alignments/Edit.rdf";
						
						equivalenceAlignmentSet.add(a);

						outputAlignment = new File(editAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment editAlignment = (BasicAlignment)(a.clone());

						try {
							editAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							editAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();
						

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {
							

							//get the alignment
							File computedAlignment = new File(editAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(editAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedEditAlignment = null;
						try {
							evaluatedEditAlignment = aparser.parse(new URI("file:"+editAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedEditAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						
						equivalenceAlignmentSet.add(evaluatedEditAlignment);  

						//evaluation
						double editFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double editPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double editRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";

						EQdataset.addValue( editPrecisionValue , "Edit" , precision );        
						EQdataset.addValue( editRecallValue , "Edit" , recall );        
						EQdataset.addValue( editFMeasureValue , "Edit" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("Edit:\n");
						sbMatchingResults.append("F-measure: " + editFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + editRecallValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + editRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}

					//smoa matcher
					if (checkBoxSmoa.isSelected()) {
						Alignment a = new SmoaMatcher_remove();
					threshold = (double)sliderEQSmoa.getValue()/100;
					

					try {
						a.init(ontoFile1.toURI(), ontoFile2.toURI());
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}
					params = new Properties();
					params.setProperty("", "");
					try {
						((AlignmentProcess) a).align((Alignment)null, params);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}	


					smoaAlignmentFileName = "./files/GUITest/alignments/Smoa.rdf";

					outputAlignment = new File(smoaAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment smoaAlignment = (BasicAlignment)(a.clone());

					try {
						smoaAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						smoaAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();

					//check domain constraint
					if (chckbxEnforceSameDomain.isSelected()) {
						

						//get the alignment
						File computedAlignment = new File(smoaAlignmentFileName);
						AlignmentParser parser = new AlignmentParser();
						BasicAlignment alignmentDomainConstraint = null;

						String concept1 = null;
						String concept2 = null;

						try {
							alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

							for (Cell c : alignmentDomainConstraint) {
								concept1 = c.getObject1AsURI().getFragment();
								concept2 = c.getObject2AsURI().getFragment();
								if (WNDomain.sameDomain(concept1, concept2)) {
									//increase by 10 percent
									c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
								} else {
									//reduce by 10 percent
									c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
								}

							}
						} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
							e1.printStackTrace();
						}


						PrintWriter writer = null;
						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(smoaAlignmentFileName)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						try {
							alignmentDomainConstraint.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

					}

					//evaluate
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {

						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedSmoaAlignment = null;
					try {
						evaluatedSmoaAlignment = aparser.parse(new URI("file:"+smoaAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedSmoaAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					
					equivalenceAlignmentSet.add(evaluatedSmoaAlignment);

					//evaluation
					double smoaFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double smoaPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double smoaRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					final String precision = "Precision";        
					final String recall = "Recall";        
					final String fMeasure = "F-Measure";

					EQdataset.addValue( smoaPrecisionValue , "Smoa" , precision );        
					EQdataset.addValue( smoaRecallValue , "Smoa" , recall );        
					EQdataset.addValue( smoaFMeasureValue , "Smoa" , fMeasure );  

					sbMatchingResults.append("\n");
					sbMatchingResults.append("Smoa:\n");
					sbMatchingResults.append("F-measure: " + smoaFMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + smoaPrecisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + smoaRecallValue);

					EQmatchingResultsPane.setText(sbMatchingResults.toString());


					}
					if (checkBoxIsub.isSelected()) {

						Alignment a = new ISubMatcher();
						threshold = (double)sliderEQISub.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						iSubAlignmentFileName = "./files/GUITest/alignments/ISub.rdf";

						outputAlignment = new File(iSubAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment iSubAlignment = (BasicAlignment)(a.clone());

						try {
							iSubAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							iSubAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {
							
							//get the alignment
							File computedAlignment = new File(iSubAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(iSubAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedISubAlignment = null;
						try {
							evaluatedISubAlignment = aparser.parse(new URI("file:"+iSubAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedISubAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						
						equivalenceAlignmentSet.add(evaluatedISubAlignment);

						//evaluation
						double iSubFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double iSubPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double iSubRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";

						EQdataset.addValue( iSubPrecisionValue , "ISub" , precision );        
						EQdataset.addValue( iSubRecallValue , "ISub" , recall );        
						EQdataset.addValue( iSubFMeasureValue , "ISub" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("ISub:\n");
						sbMatchingResults.append("F-measure: " + iSubFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + iSubPrecisionValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + iSubRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}
					if (checkBoxTrigram.isSelected()) {

						Alignment a = new TrigramMatcher();
						threshold = (double)sliderEQTrigram.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						trigramAlignmentFileName = "./files/GUITest/alignments/Trigram.rdf";

						outputAlignment = new File(trigramAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment editTrigramAlignment = (BasicAlignment)(a.clone());

						try {
							editTrigramAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							editTrigramAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {

							//get the alignment
							File computedAlignment = new File(trigramAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(trigramAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedTrigramAlignment = null;
						try {
							evaluatedTrigramAlignment = aparser.parse(new URI("file:"+trigramAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedTrigramAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						
						equivalenceAlignmentSet.add(evaluatedTrigramAlignment);
						//evaluation
						double trigramFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double trigramPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double trigramRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";


						EQdataset.addValue( trigramPrecisionValue , "Trigram" , precision );        
						EQdataset.addValue( trigramRecallValue , "Trigram" , recall );        
						EQdataset.addValue( trigramFMeasureValue , "Trigram" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("Trigram:\n");
						sbMatchingResults.append("F-measure: " + trigramFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + trigramPrecisionValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + trigramRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}

					if (checkBoxWordnet.isSelected()) {

						Alignment a = new WNRiWordNetDistance();
						threshold = (double)sliderEQWordNet.getValue()/100;
						

						try {
							a.init(ontoFile1.toURI(), ontoFile2.toURI());
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}
						params = new Properties();
						params.setProperty("", "");
						try {
							((AlignmentProcess) a).align((Alignment)null, params);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}	


						wordNetAlignmentFileName = "./files/GUITest/alignments/WordNetEQ.rdf";

						outputAlignment = new File(wordNetAlignmentFileName);

						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(outputAlignment)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						renderer = new RDFRendererVisitor(writer);

						BasicAlignment wordNetAlignment = (BasicAlignment)(a.clone());

						try {
							wordNetAlignment.cut(threshold);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						try {
							wordNetAlignment.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						//check domain constraint
						if (chckbxEnforceSameDomain.isSelected()) {
							

							//get the alignment
							File computedAlignment = new File(wordNetAlignmentFileName);
							AlignmentParser parser = new AlignmentParser();
							BasicAlignment alignmentDomainConstraint = null;

							String concept1 = null;
							String concept2 = null;

							try {
								alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

								for (Cell c : alignmentDomainConstraint) {
									concept1 = c.getObject1AsURI().getFragment();
									concept2 = c.getObject2AsURI().getFragment();
									if (WNDomain.sameDomain(concept1, concept2)) {
										//increase by 10 percent
										c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
									} else {
										//reduce by 10 percent
										c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
									}

								}
							} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
								e1.printStackTrace();
							}


							PrintWriter writer = null;
							try {
								writer = new PrintWriter(
										new BufferedWriter(
												new FileWriter(wordNetAlignmentFileName)), true);
							} catch (IOException e1) {
								e1.printStackTrace();
							} 
							AlignmentVisitor renderer = new RDFRendererVisitor(writer);

							try {
								alignmentDomainConstraint.render(renderer);
							} catch (AlignmentException e1) {
								e1.printStackTrace();
							}
							writer.flush();
							writer.close();

						}

						//evaluate
						aparser = new AlignmentParser(0);

						Alignment referenceAlignment = null;
						try {

							referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}

						Alignment evaluatedWordNetAlignment = null;
						try {
							evaluatedWordNetAlignment = aparser.parse(new URI("file:"+wordNetAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {

							e1.printStackTrace();
						}
						Properties p = new Properties();

						PRecEvaluator eval = null;
						try {
							eval = new PRecEvaluator(referenceAlignment, evaluatedWordNetAlignment);
						} catch (AlignmentException e1) {

							e1.printStackTrace();
						}

						try {
							eval.eval(p);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}

						equivalenceAlignmentSet.add(evaluatedWordNetAlignment);
						
						Alignment intersectedAlignment = null;
						
						try {
							intersectedAlignment = ParallelCombination.intersectRelaxed(equivalenceAlignmentSet);
						} catch (AlignmentException e1) {
							// FIXME Auto-generated catch block
							e1.printStackTrace();
						}
						
						/* NOTE 16.01.2018: Not working
						 * 
						 * DownloadFile df = new DownloadFile();
						
						File alignmentFile = null;
						
						try {
							alignmentFile = df.storeFile();
						} catch (Exception e1) {
							// FIXME Auto-generated catch block
							e1.printStackTrace();
						}*/
						
						//evaluation
						double wordNetFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
						double wordNetPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
						double wordNetRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

						final String precision = "Precision";        
						final String recall = "Recall";        
						final String fMeasure = "F-Measure";
	

						EQdataset.addValue( wordNetPrecisionValue , "WordNet" , precision );        
						EQdataset.addValue( wordNetRecallValue , "WordNet" , recall );        
						EQdataset.addValue( wordNetFMeasureValue , "WordNet" , fMeasure );  

						sbMatchingResults.append("\n");
						sbMatchingResults.append("WordNet:\n");
						sbMatchingResults.append("F-measure: " + wordNetFMeasureValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Precision: " + wordNetPrecisionValue);
						sbMatchingResults.append("\n");
						sbMatchingResults.append("Recall: " + wordNetRecallValue);

						EQmatchingResultsPane.setText(sbMatchingResults.toString());

					}

				

					//add chart
					
					EQchart.setBorderVisible(true);
					CategoryPlot EQcp = EQchart.getCategoryPlot();
					EQcp.setBackgroundPaint(Color.white);

					
					EQmatchingResultsChartPanel.setBorder(null);
					Dimension matchingResultsDimension = new Dimension();
					matchingResultsDimension.setSize(490, 315);
					//EQgraphJPanel.setBounds(456, 215, 491, 315);
					EQmatchingResultsChartPanel.setPreferredSize(matchingResultsDimension);
					EQgraphJPanel.add(EQmatchingResultsChartPanel);
					EQgraphJPanel.setVisible(true);  

				
			
			}
		}
	
		);
		btnCompute.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnCompute.setBounds(229, 619, 140, 29);
		panelEquivalenceMatching.add(btnCompute);
		
		JLabel lblNoteThatIf = new JLabel("Note that if more than one matcher is selected alignments are combined using intersection (simple)");
		lblNoteThatIf.setForeground(Color.RED);
		lblNoteThatIf.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblNoteThatIf.setBounds(51, 549, 523, 41);
		panelEquivalenceMatching.add(lblNoteThatIf);


		/*** Subsumption Matching ***/
		final JPanel panelsubsumptionMatching = new JPanel();
		panelsubsumptionMatching.setBackground(Color.WHITE);
		frame.getContentPane().add(panelsubsumptionMatching, "name_25771746383092");
		panelsubsumptionMatching.setLayout(null);
		panelsubsumptionMatching.setVisible(false);

		final JPanel graphJPanel = new JPanel();
		graphJPanel.setBorder(null);
		graphJPanel.setBackground(Color.WHITE);
		graphJPanel.setBounds(616, 405, 362, 234);
		panelsubsumptionMatching.add(graphJPanel);

		final JEditorPane matchingResultsPane = new JEditorPane();
		matchingResultsPane.setFont(new Font("Lucida Grande", Font.PLAIN, 6));
		matchingResultsPane.setForeground(Color.GRAY);
		matchingResultsPane.setBackground(Color.WHITE);
		matchingResultsPane.setBounds(489, 377, 86, 275);
		panelsubsumptionMatching.add(matchingResultsPane);

		final JPanel combinedProfilespanel = new JPanel();
		combinedProfilespanel.setBounds(616, 35, 362, 313);
		panelsubsumptionMatching.add(combinedProfilespanel);
		combinedProfilespanel.setBorder(null);
		combinedProfilespanel.setBackground(Color.WHITE);

		//labels
		final JLabel lblOntology1 = new JLabel("");
		lblOntology1.setEnabled(false);
		lblOntology1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblOntology1.setBounds(175, 40, 133, 16);
		panelsubsumptionMatching.add(lblOntology1);

		final JLabel lblOntology2 = new JLabel("");
		lblOntology2.setEnabled(false);
		lblOntology2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblOntology2.setBounds(175, 70, 133, 16);
		panelsubsumptionMatching.add(lblOntology2);

		final JLabel lblRefAlignment = new JLabel("");
		lblRefAlignment.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblRefAlignment.setEnabled(false);
		lblRefAlignment.setBounds(175, 102, 218, 16);
		panelsubsumptionMatching.add(lblRefAlignment);

		final JSlider sliderCompoundMatcher = new JSlider();
		sliderCompoundMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderCompoundMatcher.setBounds(185, 165, 249, 46);
		sliderCompoundMatcher.setMinorTickSpacing(2);
		sliderCompoundMatcher.setMajorTickSpacing(10);
		sliderCompoundMatcher.setPaintLabels(true);
		panelsubsumptionMatching.add(sliderCompoundMatcher);

		final JSlider sliderParentMatcher = new JSlider();
		sliderParentMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderParentMatcher.setPaintLabels(true);
		sliderParentMatcher.setMinorTickSpacing(2);
		sliderParentMatcher.setMajorTickSpacing(10);
		sliderParentMatcher.setBounds(185, 209, 249, 46);
		panelsubsumptionMatching.add(sliderParentMatcher);

		final JSlider sliderAncestorMatcher = new JSlider();
		sliderAncestorMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderAncestorMatcher.setPaintLabels(true);
		sliderAncestorMatcher.setMinorTickSpacing(2);
		sliderAncestorMatcher.setMajorTickSpacing(10);
		sliderAncestorMatcher.setBounds(185, 260, 249, 46);
		panelsubsumptionMatching.add(sliderAncestorMatcher);

		final JSlider sliderWNMatcher = new JSlider();
		sliderWNMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderWNMatcher.setPaintLabels(true);
		sliderWNMatcher.setMinorTickSpacing(2);
		sliderWNMatcher.setMajorTickSpacing(10);
		sliderWNMatcher.setBounds(185, 310, 249, 46);
		panelsubsumptionMatching.add(sliderWNMatcher);

		final JCheckBox checkBoxSameDomainConstraintMatcher = new JCheckBox("");
		checkBoxSameDomainConstraintMatcher.setBounds(6, 371, 37, 23);
		panelsubsumptionMatching.add(checkBoxSameDomainConstraintMatcher);

		//checkboxes
		final JCheckBox chckbxWeightedSequentialComposition = new JCheckBox("Weighted Sequential Composition");
		chckbxWeightedSequentialComposition.setBounds(6, 468, 257, 23);
		panelsubsumptionMatching.add(chckbxWeightedSequentialComposition);

		final JCheckBox chckbxParallelSimpleVote = new JCheckBox("Parallel Simple Vote Composition");
		chckbxParallelSimpleVote.setBounds(6, 500, 257, 23);
		panelsubsumptionMatching.add(chckbxParallelSimpleVote);

		final JCheckBox chckbxParallelPrioritised = new JCheckBox("Parallel Prioritised Composition");
		chckbxParallelPrioritised.setBounds(6, 533, 257, 23);
		panelsubsumptionMatching.add(chckbxParallelPrioritised);

		final JCheckBox chckbxHybridComposition = new JCheckBox("Hybrid Composition");
		chckbxHybridComposition.setBounds(6, 568, 257, 23);
		panelsubsumptionMatching.add(chckbxHybridComposition);

		//text areas		
		final JTextArea textAreaOntology1Profile = new JTextArea();
		textAreaOntology1Profile.setBackground(Color.WHITE);
		textAreaOntology1Profile.setFont(new Font("Times New Roman", Font.PLAIN, 10));
		textAreaOntology1Profile.setEditable(false);
		textAreaOntology1Profile.setBounds(510, 17, 37, 51);
		panelsubsumptionMatching.add(textAreaOntology1Profile);

		final JTextArea textAreaOntology2Profile = new JTextArea();
		textAreaOntology2Profile.setBackground(Color.WHITE);
		textAreaOntology2Profile.setFont(new Font("Times New Roman", Font.PLAIN, 10));
		textAreaOntology2Profile.setEditable(false);
		textAreaOntology2Profile.setBounds(559, 17, 45, 49);
		panelsubsumptionMatching.add(textAreaOntology2Profile);

		//textfields
		textFieldCompoundMatcherPriority = new JTextField();
		textFieldCompoundMatcherPriority.setBounds(449, 168, 45, 26);
		panelsubsumptionMatching.add(textFieldCompoundMatcherPriority);
		textFieldCompoundMatcherPriority.setColumns(10);

		textFieldParentMatcherPriority = new JTextField();
		textFieldParentMatcherPriority.setColumns(10);
		textFieldParentMatcherPriority.setBounds(449, 217, 45, 26);
		panelsubsumptionMatching.add(textFieldParentMatcherPriority);

		textFieldAncestorMatcherPriority = new JTextField();
		textFieldAncestorMatcherPriority.setColumns(10);
		textFieldAncestorMatcherPriority.setBounds(449, 267, 45, 26);
		panelsubsumptionMatching.add(textFieldAncestorMatcherPriority);

		textFieldWordNetMatcherPriority = new JTextField();
		textFieldWordNetMatcherPriority.setColumns(10);
		textFieldWordNetMatcherPriority.setBounds(449, 317, 45, 26);
		panelsubsumptionMatching.add(textFieldWordNetMatcherPriority);

		//dataset for charts
		final DefaultCategoryDataset dataset = 
				new DefaultCategoryDataset( );  

		//buttons

		final JButton btnDownloadAlignment = new JButton("Download alignment");

		btnDownloadAlignment.setToolTipText("");
		btnDownloadAlignment.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnDownloadAlignment.setBounds(175, 622, 153, 29);
		panelsubsumptionMatching.add(btnDownloadAlignment);

		JButton btnsubsumptionMatching = new JButton("Subsumption Matching");
		btnsubsumptionMatching.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelsubsumptionMatching.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnsubsumptionMatching.setBounds(193, 383, 175, 49);
		panelMenu.add(btnsubsumptionMatching);

		JButton btnAdvancedMatching = new JButton("Equivalence Matching");
		btnAdvancedMatching.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelEquivalenceMatching.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnAdvancedMatching.setBounds(193, 444, 180, 49);
		panelMenu.add(btnAdvancedMatching);
		
		JLabel lblOntologyMatching = new JLabel("COMPOSE Ontology Matching Framework");
		lblOntologyMatching.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		lblOntologyMatching.setBounds(193, 43, 430, 16);
		panelMenu.add(lblOntologyMatching);
		
		JLabel lblTheComposeOntology = new JLabel("The COMPOSE Ontology Matching Framework... ");
		lblTheComposeOntology.setBounds(193, 83, 532, 16);
		panelMenu.add(lblTheComposeOntology);
		
		JLabel lblMenu = new JLabel("Matching");
		lblMenu.setBounds(193, 161, 128, 16);
		panelMenu.add(lblMenu);
		
		JLabel lblEvaluation = new JLabel("Evaluation");
		lblEvaluation.setBounds(193, 346, 175, 16);
		panelMenu.add(lblEvaluation);

		JButton btnCancel = new JButton("Main menu");
		btnCancel.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelsubsumptionMatching.setVisible(false);
				panelMenu.setVisible(true);
			}
		});

		btnCancel.setBounds(344, 622, 133, 29);
		panelsubsumptionMatching.add(btnCancel);

		JButton btnUploadOntology1 = new JButton("Upload ontology 1");
		btnUploadOntology1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnUploadOntology1.setBackground(Color.LIGHT_GRAY);
		btnUploadOntology1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				OpenFile of1 = new OpenFile();

				try {
					ontoFile1 = of1.getOntoFile1();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				lblOntology1.setText(StringUtilities.stripPath(ontoFile1.toString()));

			}
		});
		btnUploadOntology1.setBounds(6, 34, 169, 29);
		panelsubsumptionMatching.add(btnUploadOntology1);

		JButton btnUploadOntology2 = new JButton("Upload ontology 2");
		btnUploadOntology2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnUploadOntology2.setBackground(Color.LIGHT_GRAY);
		btnUploadOntology2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OpenFile of2 = new OpenFile();

				try {
					ontoFile2 = of2.getOntoFile1();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				lblOntology2.setText(StringUtilities.stripPath(ontoFile2.toString()));

			}
		});
		btnUploadOntology2.setBounds(6, 66, 169, 29);
		panelsubsumptionMatching.add(btnUploadOntology2);

		JButton btnUploadReferenceAlignment = new JButton("Upload reference alignment");
		btnUploadReferenceAlignment.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnUploadOntology2.setBackground(Color.LIGHT_GRAY);
		btnUploadReferenceAlignment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OpenFile of3 = new OpenFile();

				try {
					refAlignFile = of3.getFile();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				lblRefAlignment.setText(StringUtilities.stripPath(refAlignFile.toString()));

			}
		});
		btnUploadReferenceAlignment.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnUploadReferenceAlignment.setBackground(Color.LIGHT_GRAY);
		btnUploadReferenceAlignment.setBounds(6, 98, 169, 29);
		panelsubsumptionMatching.add(btnUploadReferenceAlignment);

		JButton btnComputeProfiles = new JButton("Compute ontology profiles");
		btnComputeProfiles.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnComputeProfiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//compute profile metrics for ontology 1

				//Compound Ratio
				double cr1 = 0;

				try {
					cr1 = round(utilities.OntologyOperations.getNumClassCompounds(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}



				//Inheritance Richness
				double ir1 = 0;

				try {
					ir1 = round(OntologyProfiler.computeInheritanceRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Relationship Richness
				double rr1 = 0;

				try {
					rr1 = round(OntologyProfiler.computeRelationshipRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//WordNet Coverage
				double wc1 = 0;
				try {
					wc1 = round(OntologyProfiler.computeWordNetCoverageComp(ontoFile1), 2);
				} catch (OWLOntologyCreationException | FileNotFoundException | JWNLException e1) {
					e1.printStackTrace();
				}

				//Synonym Richness
				double sr1 = 0;

				try {
					sr1 = round(utilities.OntologyOperations.getSynonymRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Hyponym Richness
				double hr1 = 0;

				try {
					hr1 = round(utilities.OntologyOperations.getHyponymRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Domain Diversity
				double dd1 = 0;

				try {
					try {
						dd1 = round(utilities.OntologyOperations.domainDiversity(ontoFile1), 2);
					} catch (FileNotFoundException | JWNLException e1) {

						e1.printStackTrace();
					}
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				String ontology1Name = StringUtilities.stripPath(ontoFile1.toString());
				sbOntologyProfile1.append("- " + ontology1Name + " -");
				sbOntologyProfile1.append("\n");
				sbOntologyProfile1.append("Compound Ratio: " + String.valueOf(cr1));
				sbOntologyProfile1.append("\n");
				sbOntologyProfile1.append("Inheritance Richness: " + String.valueOf(ir1));
				sbOntologyProfile1.append("\n");
				sbOntologyProfile1.append("Relationship Richness: " + String.valueOf(rr1));
				sbOntologyProfile1.append("\n");
				sbOntologyProfile1.append("WordNet Coverage: " + String.valueOf(wc1));
				sbOntologyProfile1.append("\n");
				sbOntologyProfile1.append("Synonym Richness: " + String.valueOf(sr1));
				sbOntologyProfile1.append("\n");
				sbOntologyProfile1.append("Hyponym Richness: " + String.valueOf(hr1));
				sbOntologyProfile1.append("\n");
				sbOntologyProfile1.append("Domain Diversity: " + String.valueOf(dd1));


				textAreaOntology1Profile.setText(sbOntologyProfile1.toString());

				//compute profile metrics for ontology 2

				//Compound Ratio
				double cr2 = 0;

				try {
					cr2 = round(utilities.OntologyOperations.getNumClassCompounds(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}



				//Inheritance Richness
				double ir2 = 0;

				try {
					ir2 = round(OntologyProfiler.computeInheritanceRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Relationship Richness
				double rr2 = 0;

				try {
					rr2 = round(OntologyProfiler.computeRelationshipRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//WordNet Coverage
				double wc2 = 0;
				try {
					wc2 = round(OntologyProfiler.computeWordNetCoverageComp(ontoFile2), 2);
				} catch (OWLOntologyCreationException | FileNotFoundException | JWNLException e1) {
					e1.printStackTrace();
				}

				//Synonym Richness
				double sr2 = 0;

				try {
					sr2 = round(utilities.OntologyOperations.getSynonymRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Hyponym Richness
				double hr2 = 0;

				try {
					hr2 = round(utilities.OntologyOperations.getHyponymRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Domain Diversity
				double dd2 = 0;

				try {
					try {
						dd2 = round(utilities.OntologyOperations.domainDiversity(ontoFile2), 2);
					} catch (FileNotFoundException | JWNLException e1) {

						e1.printStackTrace();
					}
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				String ontology2Name = StringUtilities.stripPath(ontoFile2.toString());
				sbOntologyProfile2.append("- " + ontology2Name + " -");
				sbOntologyProfile2.append("\n");
				sbOntologyProfile2.append("Compound Ratio: " + String.valueOf(cr2));
				sbOntologyProfile2.append("\n");
				sbOntologyProfile2.append("Inheritance Richness: " + String.valueOf(ir2));
				sbOntologyProfile2.append("\n");
				sbOntologyProfile2.append("Relationship Richness: " + String.valueOf(rr2));
				sbOntologyProfile2.append("\n");
				sbOntologyProfile2.append("WordNet Coverage: " + String.valueOf(wc2));
				sbOntologyProfile2.append("\n");
				sbOntologyProfile2.append("Synonym Richness: " + String.valueOf(sr2));
				sbOntologyProfile2.append("\n");
				sbOntologyProfile2.append("Hyponym Richness: " + String.valueOf(hr2));
				sbOntologyProfile2.append("\n");
				sbOntologyProfile2.append("Domain Diversity: " + String.valueOf(dd2));


				textAreaOntology2Profile.setText(sbOntologyProfile2.toString());

				//add combined values to the graph for CR, IR, RR, WC and create series for Terminological Profile, Structural Profile and Lexical Profile
				double combinedCR = (cr1 + cr2) / 2;

				//must normalize IR somehow...
				//double combinedIR = (ir1 + ir2) / 2;
				double combinedRR = (rr1 + rr2) / 2;
				double combinedIRAndRR = (0.45 + combinedRR) / 2;
				double combinedWC = (wc1 + wc2) / 2;

				final DefaultCategoryDataset combinedProfileDataset =
						new DefaultCategoryDataset();

				combinedProfileDataset.addValue(combinedCR, "Terminological Profile", "CR");
				combinedProfileDataset.addValue(combinedIRAndRR, "Structural Profile", "IR");
				//combinedProfileDataset.addValue(combinedRR, "Structural Profile", "RR");
				combinedProfileDataset.addValue(combinedWC, "Lexical Profile", "WC");

				JFreeChart combinedProfileChart = ChartFactory.createBarChart("", "", "", combinedProfileDataset, PlotOrientation.VERTICAL, true, false, false);
				combinedProfileChart.setBorderVisible(false);
				combinedProfileChart.getLegend().setFrame(BlockBorder.NONE);
				CategoryPlot combinedProfilePlot = combinedProfileChart.getCategoryPlot();
				combinedProfilePlot.setBackgroundPaint(Color.white);

				CategoryAxis combinedProfileAxis = combinedProfilePlot.getDomainAxis();
				combinedProfileAxis.setLowerMargin(0.1);
				combinedProfileAxis.setUpperMargin(0.1);
				combinedProfileAxis.setCategoryMargin(0.1);


				ChartPanel chartPanelCombinedOntologyProfiles = new ChartPanel((JFreeChart) combinedProfileChart);
				chartPanelCombinedOntologyProfiles.setBorder(null);

				chartPanelCombinedOntologyProfiles.setBackground(Color.WHITE);
				chartPanelCombinedOntologyProfiles.setMaximumDrawHeight(224);
				chartPanelCombinedOntologyProfiles.setMaximumDrawWidth(346);
				combinedProfilespanel.add(chartPanelCombinedOntologyProfiles);
				Dimension dim = new Dimension();
				dim.setSize(346, 224);
				chartPanelCombinedOntologyProfiles.setPreferredSize(dim);
				combinedProfilespanel.setVisible(true);


			}
		});
		btnComputeProfiles.setBounds(310, 34, 194, 29);
		panelsubsumptionMatching.add(btnComputeProfiles);



		//checkboxes
		final JCheckBox checkBoxCompoundMatcher = new JCheckBox("Compound Matcher");
		checkBoxCompoundMatcher.setBounds(6, 171, 257, 23);
		panelsubsumptionMatching.add(checkBoxCompoundMatcher);

		final JCheckBox checkBoxParentMatcher = new JCheckBox("Parent Matcher");
		checkBoxParentMatcher.setBounds(6, 218, 218, 23);
		panelsubsumptionMatching.add(checkBoxParentMatcher);

		final JCheckBox checkBoxAncestorMatcher = new JCheckBox("Ancestor Matcher");
		checkBoxAncestorMatcher.setBounds(6, 268, 176, 23);
		panelsubsumptionMatching.add(checkBoxAncestorMatcher);

		final JCheckBox checkBoxWordNetMatcher = new JCheckBox("WordNet Matcher");
		checkBoxWordNetMatcher.setBounds(6, 318, 196, 23);
		panelsubsumptionMatching.add(checkBoxWordNetMatcher);



		JButton btnEvaluate = new JButton("Evaluate");
		btnEvaluate.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnEvaluate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//compound matcher
				if (checkBoxCompoundMatcher.isSelected()) {

					Alignment a = new CompoundMatcher();
					threshold = (double)sliderCompoundMatcher.getValue()/100;

					try {
						a.init(ontoFile1.toURI(), ontoFile2.toURI());
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}
					params = new Properties();
					params.setProperty("", "");
					try {
						((AlignmentProcess) a).align((Alignment)null, params);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}	


					compoundAlignmentFileName = "./files/GUITest/alignments/Compound.rdf";

					outputAlignment = new File(compoundAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment compounddAlignment = (BasicAlignment)(a.clone());

					try {
						compounddAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						compounddAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();

					//check domain constraint
					if (checkBoxSameDomainConstraintMatcher.isSelected()) {

						//get the alignment
						File computedAlignment = new File(compoundAlignmentFileName);
						AlignmentParser parser = new AlignmentParser();
						BasicAlignment alignmentDomainConstraint = null;

						String concept1 = null;
						String concept2 = null;

						try {
							alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

							for (Cell c : alignmentDomainConstraint) {
								concept1 = c.getObject1AsURI().getFragment();
								concept2 = c.getObject2AsURI().getFragment();
								if (WNDomain.sameDomain(concept1, concept2)) {
									//increase by 10 percent
									c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
								} else {
									//reduce by 10 percent
									c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
								}

							}
						} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
							e1.printStackTrace();
						}

						//store the new alignment
						//File outputAlignment = new File("./files/experiment_eswc17/alignments/biblio-bibo/web-intelligence-17-weightedSequentialCombination/structure-lexical-string.rdf");

						PrintWriter writer = null;
						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(compoundAlignmentFileName)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						try {
							alignmentDomainConstraint.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();

						/*						btnDownloadAlignment.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// Download file
						        URL url = null;
								try {
									url = new URL("http://pc.stiga.com/CNWS/IDB/Assets/Streamer.aspx?authenticationToken=AUTH21B9C67F25154B98BE13489CE6B28E6720150120105740&workgroupGUID=16B3494E-0D80-458E-AF56-9A6C3CC4A2EF&assetGUID=BCF79526-D7FD-4685-8576-4CC37B10E7E3");
								} catch (MalformedURLException e1) {
									e1.printStackTrace();
								}
						        File f = new File("");
						        try {
									org.apache.commons.io.FileUtils.copyURLToFile(url, f);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						});*/
					}

					//evaluate
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {

						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+compoundAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double compoundFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double compoundPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double compoundRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					final String precision = "Precision";        
					final String recall = "Recall";        
					final String fMeasure = "F-Measure";

					dataset.addValue( compoundPrecisionValue , "Compound" , precision );        
					dataset.addValue( compoundRecallValue , "Compound" , recall );        
					dataset.addValue( compoundFMeasureValue , "Compound" , fMeasure );  


					sbMatchingResults.append("\n");
					sbMatchingResults.append("Compound:\n");
					sbMatchingResults.append("F-measure: " + compoundFMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + compoundPrecisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + compoundRecallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());

				}

				//parent matcher (using Neo4J)
				if (checkBoxParentMatcher.isSelected()) {
					//run parent matcher with the two uploaded ontologies
					//create a new Neo4J database for each matching using a timestamp
					//return number of milliseconds since January 1, 1970, 00:00:00 GMT
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					String dbName = String.valueOf(timestamp.getTime());

					//TO-DO: SHould let the user select folder using an upload button
					File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/" + dbName);

					GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
					registerShutdownHook(db);


					String ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
					String ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());					

					//create new graphs
					manager = OWLManager.createOWLOntologyManager();
					try {
						o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
					} catch (OWLOntologyCreationException e1) {
						e1.printStackTrace();
					}
					try {
						o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
					} catch (OWLOntologyCreationException e1) {
						e1.printStackTrace();
					}
					labelO1 = DynamicLabel.label( ontologyParameter1 );
					labelO2 = DynamicLabel.label( ontologyParameter2 );

					creator = new Graph(db);
					try {
						creator.createOntologyGraph(o1, labelO1);
					} catch (OWLOntologyCreationException e1) {
						// FIXME Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						creator.createOntologyGraph(o2, labelO2);
					} catch (OWLOntologyCreationException e1) {
						// FIXME Auto-generated catch block
						e1.printStackTrace();
					}

					//perform the matching
					a = new ParentMatcher(ontologyParameter1,ontologyParameter2, db);
					threshold = (double)sliderParentMatcher.getValue()/100;
					try {
						a.init(ontoFile1.toURI(), ontoFile2.toURI());
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					params = new Properties();
					params.setProperty("", "");
					try {
						a.align((Alignment)null, params);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}	

					parentMatcherAlignmentFileName = "./files/GUITest/alignments/ParentMatcher.rdf";

					outputAlignment = new File(parentMatcherAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment parentMatcherAlignment = (BasicAlignment)(a.clone());

					try {
						parentMatcherAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						parentMatcherAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();

					//check domain constraint
					if (checkBoxSameDomainConstraintMatcher.isSelected()) {

						//get the alignment
						File computedAlignment = new File(parentMatcherAlignmentFileName);
						AlignmentParser parser = new AlignmentParser();
						BasicAlignment alignmentDomainConstraint = null;

						String concept1 = null;
						String concept2 = null;

						try {
							alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

							for (Cell c : alignmentDomainConstraint) {
								concept1 = c.getObject1AsURI().getFragment();
								concept2 = c.getObject2AsURI().getFragment();
								if (WNDomain.sameDomain(concept1, concept2)) {
									//increase by 10 percent
									c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
								} else {
									//reduce by 10 percent
									c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
								}

							}
						} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
							e1.printStackTrace();
						}


						PrintWriter writer = null;
						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(parentMatcherAlignmentFileName)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						try {
							alignmentDomainConstraint.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();


					}

					//evaluate
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;

					try {
						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+parentMatcherAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double parentMatcherFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double parentMatcherPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double parentMatcherRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					dataset.addValue( parentMatcherPrecisionValue , "Parent Matcher" , precision );        
					dataset.addValue( parentMatcherRecallValue , "Parent Matcher" , recall );        
					dataset.addValue( parentMatcherFMeasureValue , "Parent Matcher" , fMeasure );  


					sbMatchingResults.append("\n");
					sbMatchingResults.append("Parent Matcher: \n");
					sbMatchingResults.append("F-measure: " + parentMatcherFMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + parentMatcherPrecisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + parentMatcherRecallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());


				}
				if (checkBoxAncestorMatcher.isSelected()) {

					//create a new Neo4J database for each matching using a timestamp
					//return number of milliseconds since January 1, 1970, 00:00:00 GMT
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					String dbName = String.valueOf(timestamp.getTime());

					//TO-DO: should let the user select folder using an upload button
					File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/" + dbName);

					GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
					registerShutdownHook(db);

					String ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
					String ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

					//create new graphs
					manager = OWLManager.createOWLOntologyManager();
					try {
						o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
					} catch (OWLOntologyCreationException e1) {
						e1.printStackTrace();
					}
					try {
						o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
					} catch (OWLOntologyCreationException e1) {
						e1.printStackTrace();
					}
					labelO1 = DynamicLabel.label( ontologyParameter1 );
					labelO2 = DynamicLabel.label( ontologyParameter2 );

					creator = new Graph(db);
					try {
						creator.createOntologyGraph(o1, labelO1);
					} catch (OWLOntologyCreationException e1) {

						e1.printStackTrace();
					}
					try {
						creator.createOntologyGraph(o2, labelO2);
					} catch (OWLOntologyCreationException e1) {

						e1.printStackTrace();
					}

					//perform the matching
					a = new AncestorMatcher(ontologyParameter1,ontologyParameter2, db);
					threshold = (double)sliderAncestorMatcher.getValue()/100;
					try {
						a.init(ontoFile1.toURI(), ontoFile2.toURI());
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					params = new Properties();
					params.setProperty("", "");
					try {
						a.align((Alignment)null, params);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}	

					ancestorMatcherAlignmentFileName = "./files/GUITest/alignments/AncestorMatcher.rdf";

					outputAlignment = new File(ancestorMatcherAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment ancestorMatcherAlignment = (BasicAlignment)(a.clone());

					try {
						ancestorMatcherAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						ancestorMatcherAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();

					//check domain constraint
					if (checkBoxSameDomainConstraintMatcher.isSelected()) {

						//get the alignment
						File computedAlignment = new File(ancestorMatcherAlignmentFileName);
						AlignmentParser parser = new AlignmentParser();
						BasicAlignment alignmentDomainConstraint = null;

						String concept1 = null;
						String concept2 = null;

						try {
							alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

							for (Cell c : alignmentDomainConstraint) {
								concept1 = c.getObject1AsURI().getFragment();
								concept2 = c.getObject2AsURI().getFragment();
								if (WNDomain.sameDomain(concept1, concept2)) {
									//increase by 10 percent
									c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
								} else {
									//reduce by 10 percent
									c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
								}

							}
						} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
							e1.printStackTrace();
						}


						PrintWriter writer = null;
						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(ancestorMatcherAlignmentFileName)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						try {
							alignmentDomainConstraint.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();


					}

					//evaluate
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {
						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+ancestorMatcherAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double ancestorMatcherFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double ancestorMatcherPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double ancestorMatcherRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					final DefaultCategoryDataset dataset = 
							new DefaultCategoryDataset( );  

					dataset.addValue( ancestorMatcherPrecisionValue , "Ancestor Matcher" , precision );        
					dataset.addValue( ancestorMatcherRecallValue , "Ancestor Matcher" , recall );        
					dataset.addValue( ancestorMatcherFMeasureValue , "Ancestor Matcher" , fMeasure );  

					sbMatchingResults.append("\n");
					sbMatchingResults.append("Ancestor Matcher: \n");
					sbMatchingResults.append("F-measure: " + ancestorMatcherFMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + ancestorMatcherPrecisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + ancestorMatcherRecallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());

				}
				if (checkBoxWordNetMatcher.isSelected()) {

					//perform the matching
					a = new WNHyponymMatcher();
					threshold = (double)sliderWNMatcher.getValue()/100;
					try {
						a.init(ontoFile1.toURI(), ontoFile2.toURI());
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					params = new Properties();
					params.setProperty("", "");
					try {
						a.align((Alignment)null, params);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}	

					wordNetMatcherAlignmentFileName = "./files/GUITest/alignments/WordNetMatcher.rdf";

					outputAlignment = new File(wordNetMatcherAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment wordNetMatcherAlignment = (BasicAlignment)(a.clone());

					try {
						wordNetMatcherAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						wordNetMatcherAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();

					//check domain constraint
					if (checkBoxSameDomainConstraintMatcher.isSelected()) {

						//get the alignment
						File computedAlignment = new File(wordNetMatcherAlignmentFileName);
						AlignmentParser parser = new AlignmentParser();
						BasicAlignment alignmentDomainConstraint = null;

						String concept1 = null;
						String concept2 = null;

						try {
							alignmentDomainConstraint = (BasicAlignment)parser.parse(computedAlignment.toURI().toString());

							for (Cell c : alignmentDomainConstraint) {
								concept1 = c.getObject1AsURI().getFragment();
								concept2 = c.getObject2AsURI().getFragment();
								if (WNDomain.sameDomain(concept1, concept2)) {
									//increase by 10 percent
									c.setStrength(AlignmentOperations.increaseCellStrength(c.getStrength(), 10.0));
								} else {
									//reduce by 10 percent
									c.setStrength(AlignmentOperations.reduceCellStrength(c.getStrength(), 10.0));
								}

							}
						} catch (AlignmentException | FileNotFoundException | JWNLException e1) {
							e1.printStackTrace();
						}


						PrintWriter writer = null;
						try {
							writer = new PrintWriter(
									new BufferedWriter(
											new FileWriter(wordNetMatcherAlignmentFileName)), true);
						} catch (IOException e1) {
							e1.printStackTrace();
						} 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						try {
							alignmentDomainConstraint.render(renderer);
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
						writer.flush();
						writer.close();


					}

					//evaluate
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {
						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+wordNetMatcherAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double wordNetMatcherFMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double wordNetMatcherPrecisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double wordNetMatcherRecallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";


					dataset.addValue( wordNetMatcherPrecisionValue , "WordNet" , precision );        
					dataset.addValue( wordNetMatcherRecallValue , "WordNet" , recall );        
					dataset.addValue( wordNetMatcherFMeasureValue , "WordNet" , fMeasure );  

					sbMatchingResults.append("\n");
					sbMatchingResults.append("WordNet Matcher: \n");
					sbMatchingResults.append("F-measure: " + wordNetMatcherFMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + wordNetMatcherPrecisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + wordNetMatcherRecallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());

				}
				
				//ALIGNMENT COMBINATION METHODS

				if (chckbxWeightedSequentialComposition.isSelected()) {

					Alignment computedAlignment = null;
					String weightedSequentialCompositionAlignmentFileName = "./files/GUITest/alignments/WeightedSequentialComposition.rdf";
					
					ArrayList<Alignment> alignments = new ArrayList<Alignment>();

					//get the alignment files involved
					if (checkBoxCompoundMatcher.isSelected()) {	
						Alignment cAlignment = null;
						try {
							cAlignment = aparser.parse(new URI("file:"+compoundAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(cAlignment);
					}

					if (checkBoxParentMatcher.isSelected()) {
						Alignment parAlignment = null;
						try {
							parAlignment = aparser.parse(new URI("file:"+parentMatcherAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(parAlignment);
					}

					if (checkBoxAncestorMatcher.isSelected()) {
						Alignment ancAlignment = null;
						try {
							ancAlignment = aparser.parse(new URI("file:"+ancestorMatcherAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(ancAlignment);
					}

					if (checkBoxWordNetMatcher.isSelected()) {
						Alignment wnAlignment = null;
						try {
							wnAlignment = aparser.parse(new URI("file:"+wordNetMatcherAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(wnAlignment);
					}

					try {
						computedAlignment = ParallelCombination.simpleVote(alignments);
					} catch (AlignmentException e2) {
						e2.printStackTrace();
					}


					outputAlignment = new File(weightedSequentialCompositionAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment wscAlignment = (BasicAlignment)(computedAlignment.clone());

					try {
						wscAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						wscAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();




					//evaluate computed alignment
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {
						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+weightedSequentialCompositionAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double fMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double precisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double recallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					dataset.addValue( precisionValue , "WSC" , precision );        
					dataset.addValue( recallValue , "WSC" , recall );        
					dataset.addValue( fMeasureValue , "WSC" , fMeasure );  

					/*					//add chart
					JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, true, false, false);
					chart.setBorderVisible(true);
					CategoryPlot cp = chart.getCategoryPlot();
					cp.setBackgroundPaint(Color.white);

					ChartPanel matchingResultsChartPanel = new ChartPanel((JFreeChart) chart);
					matchingResultsChartPanel.setBorder(null);
					Dimension matchingResultsDimension = new Dimension();
					matchingResultsDimension.setSize(362, 224);
					matchingResultsChartPanel.setPreferredSize(matchingResultsDimension);
					graphJPanel.add(matchingResultsChartPanel);
					graphJPanel.setVisible(true);  */

					sbMatchingResults.append("\n");
					sbMatchingResults.append("Weighted Sequential: \n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());

				}

				if (chckbxParallelSimpleVote.isSelected()) {

					Alignment computedAlignment = null;
					String simpleVoteCompositionAlignmentFileName = "./files/GUITest/alignments/SimpleVoteComposition.rdf";
					//Set<Alignment> alignments = new HashSet<Alignment>();
					ArrayList<Alignment> alignments = new ArrayList<Alignment>();


					//get the alignment files involved
					if (checkBoxCompoundMatcher.isSelected()) {	
						Alignment cAlignment = null;
						try {
							cAlignment = aparser.parse(new URI("file:"+compoundAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(cAlignment);
					}

					if (checkBoxParentMatcher.isSelected()) {
						Alignment parAlignment = null;
						try {
							parAlignment = aparser.parse(new URI("file:"+parentMatcherAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(parAlignment);
					}

					if (checkBoxAncestorMatcher.isSelected()) {
						Alignment ancAlignment = null;
						try {
							ancAlignment = aparser.parse(new URI("file:"+ancestorMatcherAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(ancAlignment);
					}

					if (checkBoxWordNetMatcher.isSelected()) {
						Alignment wnAlignment = null;
						try {
							wnAlignment = aparser.parse(new URI("file:"+wordNetMatcherAlignmentFileName));
						} catch (AlignmentException | URISyntaxException e1) {
							e1.printStackTrace();
						}
						alignments.add(wnAlignment);
					}

					try {
						computedAlignment = ParallelCombination.simpleVote(alignments);
					} catch (AlignmentException e2) {
						e2.printStackTrace();
					}


					outputAlignment = new File(simpleVoteCompositionAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment svAlignment = (BasicAlignment)(computedAlignment.clone());

					try {
						svAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						svAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();




					//evaluate computed alignment
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {
						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+simpleVoteCompositionAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double fMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double precisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double recallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					dataset.addValue( precisionValue , "Simple Vote" , precision );        
					dataset.addValue( recallValue , "Simple Vote" , recall );        
					dataset.addValue( fMeasureValue , "Simple Vote" , fMeasure );  

					sbMatchingResults.append("\n");
					sbMatchingResults.append("Simple Vote: \n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());

				}

				if (chckbxParallelPrioritised.isSelected()) {

					//get the priorities
					int compoundPriority = Integer.parseInt(textFieldCompoundMatcherPriority.getText());
					int parentMatcherPriority = Integer.parseInt(textFieldParentMatcherPriority.getText());
					int ancestorMatcherPriority = Integer.parseInt(textFieldAncestorMatcherPriority.getText());
					int wordNetMatcherPriority = Integer.parseInt(textFieldWordNetMatcherPriority.getText());

					Alignment computedAlignment = null;
					String parallelPrioritisedCompositionAlignmentFileName = "./files/GUITest/alignments/ParallelPriorityComposition.rdf";

					ArrayList<File> alignmentFiles = new ArrayList<File>();

					//get the alignment files involved
					if (checkBoxCompoundMatcher.isSelected()) {
						File f = new File(compoundAlignmentFileName);
						alignmentFiles.add(f);
					}

					if (checkBoxParentMatcher.isSelected()) {
						File f = new File(parentMatcherAlignmentFileName);
						alignmentFiles.add(f);
					}

					if (checkBoxAncestorMatcher.isSelected()) {
						File f = new File(ancestorMatcherAlignmentFileName);
						alignmentFiles.add(f);
					}

					if (checkBoxWordNetMatcher.isSelected()) {
						File f = new File(wordNetMatcherAlignmentFileName);
						alignmentFiles.add(f);
					}

					if (alignmentFiles.size() == 3) {
						try {
							if (compoundPriority == 1) {
								computedAlignment = ParallelCombination.completeMatchWithPriority3(alignmentFiles.get(0), alignmentFiles.get(1), alignmentFiles.get(2));
							} else if (parentMatcherPriority == 1) {
								computedAlignment = ParallelCombination.completeMatchWithPriority3(alignmentFiles.get(1), alignmentFiles.get(0), alignmentFiles.get(2));
							} else  {
								computedAlignment = ParallelCombination.completeMatchWithPriority3(alignmentFiles.get(2), alignmentFiles.get(0), alignmentFiles.get(1));	
							}
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
					} else {
						try {
							if (compoundPriority == 1) {
								computedAlignment = ParallelCombination.completeMatchWithPriority4(alignmentFiles.get(0), alignmentFiles.get(1), alignmentFiles.get(2), alignmentFiles.get(3));
							} else if (parentMatcherPriority == 1) {
								computedAlignment = ParallelCombination.completeMatchWithPriority4(alignmentFiles.get(1), alignmentFiles.get(0), alignmentFiles.get(2), alignmentFiles.get(3));
							} else if (ancestorMatcherPriority == 1) {
								computedAlignment = ParallelCombination.completeMatchWithPriority4(alignmentFiles.get(2), alignmentFiles.get(0), alignmentFiles.get(1), alignmentFiles.get(3));
							} else {
								computedAlignment = ParallelCombination.completeMatchWithPriority4(alignmentFiles.get(3), alignmentFiles.get(0), alignmentFiles.get(1), alignmentFiles.get(2));
							}
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
					}

					outputAlignment = new File(parallelPrioritisedCompositionAlignmentFileName);

					try {
						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true);
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
					renderer = new RDFRendererVisitor(writer);

					BasicAlignment ppAlignment = (BasicAlignment)(computedAlignment.clone());

					try {
						ppAlignment.cut(threshold);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					try {
						ppAlignment.render(renderer);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}
					writer.flush();
					writer.close();




					//evaluate computed alignment
					aparser = new AlignmentParser(0);

					Alignment referenceAlignment = null;
					try {
						referenceAlignment = aparser.parse(new URI("file:"+refAlignFile));
					} catch (AlignmentException | URISyntaxException e1) {
						e1.printStackTrace();
					}

					Alignment evaluatedAlignment = null;
					try {
						evaluatedAlignment = aparser.parse(new URI("file:"+parallelPrioritisedCompositionAlignmentFileName));
					} catch (AlignmentException | URISyntaxException e1) {

						e1.printStackTrace();
					}
					Properties p = new Properties();

					PRecEvaluator eval = null;
					try {
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
					} catch (AlignmentException e1) {

						e1.printStackTrace();
					}

					try {
						eval.eval(p);
					} catch (AlignmentException e1) {
						e1.printStackTrace();
					}

					//evaluation
					double fMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double precisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double recallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					dataset.addValue( precisionValue , "Priority" , precision );        
					dataset.addValue( recallValue , "Priority" , recall );        
					dataset.addValue( fMeasureValue , "Priority" , fMeasure );  


					sbMatchingResults.append("\n");
					sbMatchingResults.append("Parallel Priority: \n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());




				}

				//add chart
				JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, true, false, false);
				chart.setBorderVisible(true);
				CategoryPlot cp = chart.getCategoryPlot();
				cp.setBackgroundPaint(Color.white);

				ChartPanel matchingResultsChartPanel = new ChartPanel((JFreeChart) chart);
				matchingResultsChartPanel.setBorder(null);
				Dimension matchingResultsDimension = new Dimension();
				matchingResultsDimension.setSize(362, 224);
				matchingResultsChartPanel.setPreferredSize(matchingResultsDimension);
				graphJPanel.add(matchingResultsChartPanel);
				graphJPanel.setVisible(true);  

			}
		});




		btnEvaluate.setToolTipText("");
		btnEvaluate.setBounds(6, 623, 153, 29);
		panelsubsumptionMatching.add(btnEvaluate);



		//labels
		JLabel lblUploadFiles = new JLabel("Upload files");
		lblUploadFiles.setForeground(Color.RED);
		lblUploadFiles.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblUploadFiles.setBounds(20, 13, 133, 16);
		panelsubsumptionMatching.add(lblUploadFiles);

		JLabel matchersLabel = new JLabel("Select Matchers");
		matchersLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		matchersLabel.setForeground(Color.RED);
		matchersLabel.setBounds(20, 150, 139, 16);
		panelsubsumptionMatching.add(matchersLabel);


		JLabel individualProfilesLabel = new JLabel("Ontology Profiles ");
		individualProfilesLabel.setForeground(Color.RED);
		individualProfilesLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		individualProfilesLabel.setBounds(713, 13, 299, 16);
		panelsubsumptionMatching.add(individualProfilesLabel);

		JLabel matchingResultsLabel = new JLabel("Evaluation Results");
		matchingResultsLabel.setForeground(Color.RED);
		matchingResultsLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		matchingResultsLabel.setBounds(748, 377, 132, 16);
		panelsubsumptionMatching.add(matchingResultsLabel);

		JLabel lblConfidence = new JLabel("Choose Confidence threshold");
		lblConfidence.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblConfidence.setForeground(Color.RED);
		lblConfidence.setBounds(197, 150, 218, 16);
		panelsubsumptionMatching.add(lblConfidence);

		JLabel labelSameDomainConstraint = new JLabel("Enforce same domain constraint");
		labelSameDomainConstraint.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		labelSameDomainConstraint.setToolTipText("The \"Same Domain Constraint\" adds confidence if two concepts from the ontologies being matched are associated with the same WordNet Domain and reduces confidence if they aren´t");
		labelSameDomainConstraint.setForeground(Color.RED);
		labelSameDomainConstraint.setBounds(39, 374, 163, 16);
		panelsubsumptionMatching.add(labelSameDomainConstraint);

		JLabel lblPriority = new JLabel("Set Priority (0 if not included)");
		lblPriority.setForeground(Color.RED);
		lblPriority.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblPriority.setBounds(446, 150, 147, 16);
		panelsubsumptionMatching.add(lblPriority);



		JLabel lblComposition = new JLabel("Select Composition ");
		lblComposition.setForeground(Color.RED);
		lblComposition.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblComposition.setBounds(20, 443, 395, 16);
		panelsubsumptionMatching.add(lblComposition);



		JLabel lblComputeOntologyProfile = new JLabel("Compute ontology profile");
		lblComputeOntologyProfile.setForeground(Color.RED);
		lblComputeOntologyProfile.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblComputeOntologyProfile.setBounds(318, 13, 194, 16);
		panelsubsumptionMatching.add(lblComputeOntologyProfile);

		JLabel lblConfigureAutomatically = new JLabel("Automatic matcher configuration");
		lblConfigureAutomatically.setToolTipText("The \"Same Domain Constraint\" adds confidence if two concepts from the ontologies being matched are associated with the same WordNet Domain and reduces confidence if they aren´t");
		lblConfigureAutomatically.setForeground(Color.RED);
		lblConfigureAutomatically.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblConfigureAutomatically.setBounds(340, 74, 182, 16);
		panelsubsumptionMatching.add(lblConfigureAutomatically);

		JCheckBox checkBoxConfigureMatchersAutomatically = new JCheckBox("");
		checkBoxConfigureMatchersAutomatically.setBounds(310, 70, 37, 23);
		panelsubsumptionMatching.add(checkBoxConfigureMatchersAutomatically);

		JLabel lblrequiresMin = new JLabel("(Requires min 3 alignments)");
		lblrequiresMin.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblrequiresMin.setForeground(Color.RED);
		lblrequiresMin.setBounds(259, 472, 194, 16);
		panelsubsumptionMatching.add(lblrequiresMin);

		JLabel label = new JLabel("(Requires min 3 alignments)");
		label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		label.setForeground(Color.RED);
		label.setBounds(259, 503, 194, 16);
		panelsubsumptionMatching.add(label);

		JLabel label_1 = new JLabel("(Requires min 3 alignments)");
		label_1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		label_1.setForeground(Color.RED);
		label_1.setBounds(259, 537, 194, 16);
		panelsubsumptionMatching.add(label_1);


	}

	@Override
	public Dimension getPreferredSize() {
		int w = 362;
		int h = 224;
		// given some values of w & h
		return new Dimension(w, h);
	}

	//graphJPanel.setBounds(20, 339, 286, 212);

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private static void registerShutdownHook(final GraphDatabaseService db)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();

			}
		} );
	}

	class CustomRenderer extends BarRenderer {

		/** The colors. */
		private Paint[] colors;

		/**
		 * Creates a new renderer.
		 *
		 * @param colors  the colors.
		 */
		public CustomRenderer(final Paint[] colors) {
			this.colors = colors;
		}

		/**
		 * Returns the paint for an item.  Overrides the default behaviour inherited from
		 * AbstractSeriesRenderer.
		 *
		 * @param row  the series.
		 * @param column  the category.
		 *
		 * @return The item color.
		 */
		public Paint getItemPaint(final int row, final int column) {
			return this.colors[column % this.colors.length];
		}
	}
}
