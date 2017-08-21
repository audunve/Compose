package no.ntnu.idi.compose.ui;
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

import compose.combination.SequentialComposition;
import compose.graph.GraphCreator;
import compose.matchers.CompoundMatcher;
import compose.matchers.AncestorMatcher;
import compose.matchers.ParentMatcher;
import compose.matchers.Subsumption_WordNet_Matcher;
import compose.misc.AlignmentOperations;
import compose.misc.StringUtils;
import compose.statistics.OntologyStatistics;
import compose.wordnet.WNDomain;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import net.didion.jwnl.JWNLException;
import no.ntnu.idi.compose.profiling.OntologyProcessor;

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
	private JPanel panelASimpleMatching;
	private JPanel panelAdvancedMatching;
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
	GraphCreator creator;
	private JTextField textFieldCompoundMatcherPriority;
	private JTextField textFieldParentMatcherPriority;
	private JTextField textFieldAncestorMatcherPriority;
	private JTextField textFieldWordNetMatcherPriority;
	
	String compoundAlignmentFileName = null;
	String parentMatcherAlignmentFileName = null;
	String ancestorMatcherAlignmentFileName = null;
	String wordNetMatcherAlignmentFileName = null;



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

		final JPanel panelAdvancedMatching = new JPanel();
		frame.getContentPane().add(panelAdvancedMatching, "name_25766809915035");
		panelAdvancedMatching.setLayout(null);
		panelAdvancedMatching.setVisible(false);

		final JPanel panelSimpleMatching = new JPanel();
		panelSimpleMatching.setBackground(Color.WHITE);
		frame.getContentPane().add(panelSimpleMatching, "name_25771746383092");
		panelSimpleMatching.setLayout(null);
		panelSimpleMatching.setVisible(false);

		final JPanel graphJPanel = new JPanel();
		graphJPanel.setBorder(null);
		graphJPanel.setBackground(Color.WHITE);
		graphJPanel.setBounds(616, 428, 362, 224);
		panelSimpleMatching.add(graphJPanel);

		final JPanel combinedProfilespanel = new JPanel();
		combinedProfilespanel.setBounds(616, 43, 362, 313);
		panelSimpleMatching.add(combinedProfilespanel);
		combinedProfilespanel.setBorder(null);
		combinedProfilespanel.setBackground(Color.WHITE);

		final JEditorPane matchingResultsPane = new JEditorPane();
		matchingResultsPane.setFont(new Font("Lucida Grande", Font.PLAIN, 8));
		matchingResultsPane.setForeground(Color.GRAY);
		matchingResultsPane.setBackground(Color.WHITE);
		matchingResultsPane.setBounds(489, 377, 86, 156);
		panelSimpleMatching.add(matchingResultsPane);

		//labels
		final JLabel lblOntology1 = new JLabel("");
		lblOntology1.setEnabled(false);
		lblOntology1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblOntology1.setBounds(175, 40, 133, 16);
		panelSimpleMatching.add(lblOntology1);

		final JLabel lblOntology2 = new JLabel("");
		lblOntology2.setEnabled(false);
		lblOntology2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblOntology2.setBounds(175, 70, 133, 16);
		panelSimpleMatching.add(lblOntology2);

		final JLabel lblRefAlignment = new JLabel("");
		lblRefAlignment.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblRefAlignment.setEnabled(false);
		lblRefAlignment.setBounds(175, 102, 133, 16);
		panelSimpleMatching.add(lblRefAlignment);

		final JSlider sliderCompoundMatcher = new JSlider();
		sliderCompoundMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderCompoundMatcher.setBounds(185, 165, 249, 46);
		sliderCompoundMatcher.setMinorTickSpacing(2);
		sliderCompoundMatcher.setMajorTickSpacing(10);
		sliderCompoundMatcher.setPaintLabels(true);
		panelSimpleMatching.add(sliderCompoundMatcher);

		final JSlider sliderParentMatcher = new JSlider();
		sliderParentMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderParentMatcher.setPaintLabels(true);
		sliderParentMatcher.setMinorTickSpacing(2);
		sliderParentMatcher.setMajorTickSpacing(10);
		sliderParentMatcher.setBounds(185, 209, 249, 46);
		panelSimpleMatching.add(sliderParentMatcher);

		final JSlider sliderAncestorMatcher = new JSlider();
		sliderAncestorMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderAncestorMatcher.setPaintLabels(true);
		sliderAncestorMatcher.setMinorTickSpacing(2);
		sliderAncestorMatcher.setMajorTickSpacing(10);
		sliderAncestorMatcher.setBounds(185, 260, 249, 46);
		panelSimpleMatching.add(sliderAncestorMatcher);

		final JSlider sliderWNMatcher = new JSlider();
		sliderWNMatcher.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		sliderWNMatcher.setPaintLabels(true);
		sliderWNMatcher.setMinorTickSpacing(2);
		sliderWNMatcher.setMajorTickSpacing(10);
		sliderWNMatcher.setBounds(185, 310, 249, 46);
		panelSimpleMatching.add(sliderWNMatcher);

		final JCheckBox checkBoxSameDomainConstraintMatcher = new JCheckBox("");
		checkBoxSameDomainConstraintMatcher.setBounds(6, 371, 37, 23);
		panelSimpleMatching.add(checkBoxSameDomainConstraintMatcher);

		//checkboxes
		final JCheckBox chckbxWeightedSequentialComposition = new JCheckBox("Weighted Sequential Composition");
		chckbxWeightedSequentialComposition.setBounds(6, 468, 257, 23);
		panelSimpleMatching.add(chckbxWeightedSequentialComposition);

		final JCheckBox chckbxParallelSimpleVote = new JCheckBox("Parallel Simple Vote Composition");
		chckbxParallelSimpleVote.setBounds(6, 500, 257, 23);
		panelSimpleMatching.add(chckbxParallelSimpleVote);

		final JCheckBox chckbxParallelPrioritised = new JCheckBox("Parallel Prioritised Composition");
		chckbxParallelPrioritised.setBounds(6, 533, 257, 23);
		panelSimpleMatching.add(chckbxParallelPrioritised);

		final JCheckBox chckbxHybridComposition = new JCheckBox("Hybrid Composition");
		chckbxHybridComposition.setBounds(6, 568, 257, 23);
		panelSimpleMatching.add(chckbxHybridComposition);

		//text areas		
		final JTextArea textAreaOntology1Profile = new JTextArea();
		textAreaOntology1Profile.setBackground(Color.WHITE);
		textAreaOntology1Profile.setFont(new Font("Times New Roman", Font.PLAIN, 10));
		textAreaOntology1Profile.setEditable(false);
		textAreaOntology1Profile.setBounds(510, 17, 37, 51);
		panelSimpleMatching.add(textAreaOntology1Profile);

		final JTextArea textAreaOntology2Profile = new JTextArea();
		textAreaOntology2Profile.setBackground(Color.WHITE);
		textAreaOntology2Profile.setFont(new Font("Times New Roman", Font.PLAIN, 10));
		textAreaOntology2Profile.setEditable(false);
		textAreaOntology2Profile.setBounds(559, 17, 45, 49);
		panelSimpleMatching.add(textAreaOntology2Profile);

		//buttons

		final JButton btnDownloadAlignment = new JButton("Download alignment");

		btnDownloadAlignment.setToolTipText("");
		btnDownloadAlignment.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnDownloadAlignment.setBounds(175, 622, 153, 29);
		panelSimpleMatching.add(btnDownloadAlignment);

		JButton btnSimpleMatching = new JButton("Simple Matching");
		btnSimpleMatching.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelSimpleMatching.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnSimpleMatching.setBounds(193, 71, 143, 49);
		panelMenu.add(btnSimpleMatching);

		JButton btnAdvancedMatching = new JButton("Advanced Matching");
		btnAdvancedMatching.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelAdvancedMatching.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnAdvancedMatching.setBounds(442, 71, 143, 49);
		panelMenu.add(btnAdvancedMatching);

		JButton btnAdvanceMatchingCancel = new JButton("Cancel");
		btnAdvanceMatchingCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				panelAdvancedMatching.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnAdvanceMatchingCancel.setBounds(247, 183, 117, 29);
		panelAdvancedMatching.add(btnAdvanceMatchingCancel);

		JButton btnCancel = new JButton("Main menu");
		btnCancel.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelSimpleMatching.setVisible(false);
				panelMenu.setVisible(true);
			}
		});

		btnCancel.setBounds(344, 622, 133, 29);
		panelSimpleMatching.add(btnCancel);

		JButton btnUploadOntology1 = new JButton("Upload ontology 1...");
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

				lblOntology1.setText(StringUtils.stripPath(ontoFile1.toString()));

			}
		});
		btnUploadOntology1.setBounds(6, 34, 169, 29);
		panelSimpleMatching.add(btnUploadOntology1);

		JButton btnUploadOntology2 = new JButton("Upload ontology 2...");
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

				lblOntology2.setText(StringUtils.stripPath(ontoFile2.toString()));

			}
		});
		btnUploadOntology2.setBounds(6, 66, 169, 29);
		panelSimpleMatching.add(btnUploadOntology2);

		JButton btnUploadReferenceAlignment = new JButton("Upload reference alignment...");
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
				lblRefAlignment.setText(StringUtils.stripPath(refAlignFile.toString()));

			}
		});
		btnUploadReferenceAlignment.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnUploadReferenceAlignment.setBackground(Color.LIGHT_GRAY);
		btnUploadReferenceAlignment.setBounds(6, 98, 169, 29);
		panelSimpleMatching.add(btnUploadReferenceAlignment);

		JButton btnComputeProfiles = new JButton("Compute ontology profiles");
		btnComputeProfiles.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		btnComputeProfiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//compute profile metrics for ontology 1

				//Compound Ratio
				double cr1 = 0;

				try {
					cr1 = round(OntologyStatistics.getNumClassCompounds(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}



				//Inheritance Richness
				double ir1 = 0;

				try {
					ir1 = round(OntologyProcessor.computeInheritanceRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Relationship Richness
				double rr1 = 0;

				try {
					rr1 = round(OntologyProcessor.computeRelationshipRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//WordNet Coverage
				double wc1 = 0;
				try {
					wc1 = round(OntologyProcessor.computeWordNetCoverage(ontoFile1), 2);
				} catch (OWLOntologyCreationException | FileNotFoundException | JWNLException e1) {
					e1.printStackTrace();
				}

				//Synonym Richness
				double sr1 = 0;

				try {
					sr1 = round(OntologyStatistics.getSynonymRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Hyponym Richness
				double hr1 = 0;

				try {
					hr1 = round(OntologyStatistics.getHyponymRichness(ontoFile1), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Domain Diversity
				double dd1 = 0;

				try {
					try {
						dd1 = round(OntologyStatistics.domainDiversity(ontoFile1), 2);
					} catch (FileNotFoundException | JWNLException e1) {

						e1.printStackTrace();
					}
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				String ontology1Name = StringUtils.stripPath(ontoFile1.toString());
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
					cr2 = round(OntologyStatistics.getNumClassCompounds(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}



				//Inheritance Richness
				double ir2 = 0;

				try {
					ir2 = round(OntologyProcessor.computeInheritanceRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Relationship Richness
				double rr2 = 0;

				try {
					rr2 = round(OntologyProcessor.computeRelationshipRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//WordNet Coverage
				double wc2 = 0;
				try {
					wc2 = round(OntologyProcessor.computeWordNetCoverage(ontoFile2), 2);
				} catch (OWLOntologyCreationException | FileNotFoundException | JWNLException e1) {
					e1.printStackTrace();
				}

				//Synonym Richness
				double sr2 = 0;

				try {
					sr2 = round(OntologyStatistics.getSynonymRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Hyponym Richness
				double hr2 = 0;

				try {
					hr2 = round(OntologyStatistics.getHyponymRichness(ontoFile2), 2);
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				//Domain Diversity
				double dd2 = 0;

				try {
					try {
						dd2 = round(OntologyStatistics.domainDiversity(ontoFile2), 2);
					} catch (FileNotFoundException | JWNLException e1) {

						e1.printStackTrace();
					}
				} catch (OWLOntologyCreationException e2) {

					e2.printStackTrace();
				}


				String ontology2Name = StringUtils.stripPath(ontoFile2.toString());
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
		btnComputeProfiles.setBounds(318, 34, 194, 29);
		panelSimpleMatching.add(btnComputeProfiles);



		//checkboxes
		final JCheckBox checkBoxCompoundMatcher = new JCheckBox("Compound Matcher");
		checkBoxCompoundMatcher.setBounds(6, 171, 257, 23);
		panelSimpleMatching.add(checkBoxCompoundMatcher);

		final JCheckBox checkBoxParentMatcher = new JCheckBox("Parent Matcher");
		checkBoxParentMatcher.setBounds(6, 218, 218, 23);
		panelSimpleMatching.add(checkBoxParentMatcher);

		final JCheckBox checkBoxAncestorMatcher = new JCheckBox("Ancestor Matcher");
		checkBoxAncestorMatcher.setBounds(6, 268, 176, 23);
		panelSimpleMatching.add(checkBoxAncestorMatcher);

		final JCheckBox checkBoxWordNetMatcher = new JCheckBox("WordNet Matcher");
		checkBoxWordNetMatcher.setBounds(6, 318, 196, 23);
		panelSimpleMatching.add(checkBoxWordNetMatcher);



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

					/*//evaluation
					double fMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double precisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double recallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					final String precision = "Precision";        
					final String recall = "Recall";        
					final String fMeasure = "F-Measure";

					final DefaultCategoryDataset dataset = 
							new DefaultCategoryDataset( );  

					dataset.addValue( precisionValue , "eval" , precision );        
					dataset.addValue( recallValue , "eval" , recall );        
					dataset.addValue( fMeasureValue , "eval" , fMeasure );  


					//add chart
					JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
					chart.setBorderVisible(false);
					CategoryPlot cp = chart.getCategoryPlot();
					cp.setBackgroundPaint(Color.white);

					ChartPanel matchingResultsChartPanel = new ChartPanel((JFreeChart) chart);
					matchingResultsChartPanel.setBorder(null);
					Dimension matchingResultsDimension = new Dimension();
					matchingResultsDimension.setSize(362, 224);
					matchingResultsChartPanel.setPreferredSize(matchingResultsDimension);
					//matchingResultsChartPanel.setBackground(Color.gray);
					graphJPanel.add(matchingResultsChartPanel);
					graphJPanel.setVisible(true);

					sbMatchingResults.append("\n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());*/

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


					String ontologyParameter1 = StringUtils.stripPath(ontoFile1.toString());
					String ontologyParameter2 = StringUtils.stripPath(ontoFile2.toString());					

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

					creator = new GraphCreator(db);
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

					/*//evaluate
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
					double fMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double precisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double recallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					final DefaultCategoryDataset dataset = 
							new DefaultCategoryDataset( );  

					dataset.addValue( precisionValue , "eval" , precision );        
					dataset.addValue( recallValue , "eval" , recall );        
					dataset.addValue( fMeasureValue , "eval" , fMeasure );  

					//add chart
					JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
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

					sbMatchingResults.append("\n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());*/


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

					String ontologyParameter1 = StringUtils.stripPath(ontoFile1.toString());
					String ontologyParameter2 = StringUtils.stripPath(ontoFile2.toString());

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

					creator = new GraphCreator(db);
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

					/*//evaluate
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
					double fMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double precisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double recallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					final DefaultCategoryDataset dataset = 
							new DefaultCategoryDataset( );  

					dataset.addValue( precisionValue , "eval" , precision );        
					dataset.addValue( recallValue , "eval" , recall );        
					dataset.addValue( fMeasureValue , "eval" , fMeasure );  

					//add chart
					JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
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

					sbMatchingResults.append("\n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());*/

				}
				if (checkBoxWordNetMatcher.isSelected()) {

					//perform the matching
					a = new Subsumption_WordNet_Matcher();
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

					/*//evaluate
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
					double fMeasureValue = round(Double.parseDouble(eval.getResults().getProperty("fmeasure").toString()),2);
					double precisionValue = round(Double.parseDouble(eval.getResults().getProperty("precision").toString()), 2);
					double recallValue = round(Double.parseDouble(eval.getResults().getProperty("recall").toString()), 2);

					String precision = "Precision";        
					String recall = "Recall";        
					String fMeasure = "F-Measure";

					final DefaultCategoryDataset dataset = 
							new DefaultCategoryDataset( );  

					dataset.addValue( precisionValue , "eval" , precision );        
					dataset.addValue( recallValue , "eval" , recall );        
					dataset.addValue( fMeasureValue , "eval" , fMeasure );  

					//add chart
					JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
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


					sbMatchingResults.append("\n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());*/

				}
				
				if (chckbxWeightedSequentialComposition.isSelected()) {
					
					Alignment computedAlignment = null;
					String weightedSequentialCompositionAlignmentFileName = "./files/GUITest/alignments/WeightedSequentialComposition.rdf";
					
					//Set<File> alignmentFiles = new HashSet<File>();
					ArrayList<File> alignmentFiles = new ArrayList<File>();
					
					//get the alignment files involved
					if (checkBoxCompoundMatcher.isSelected()) {
						File f1 = new File(compoundAlignmentFileName);
						alignmentFiles.add(f1);
					}
					
					if (checkBoxParentMatcher.isSelected()) {
						File f1 = new File(parentMatcherAlignmentFileName);
						alignmentFiles.add(f1);
					}
					
					if (checkBoxAncestorMatcher.isSelected()) {
						File f1 = new File(ancestorMatcherAlignmentFileName);
						alignmentFiles.add(f1);
					}
					
					if (checkBoxWordNetMatcher.isSelected()) {
						File f1 = new File(wordNetMatcherAlignmentFileName);
						alignmentFiles.add(f1);
					}
					
					if (alignmentFiles.size() == 3) {
						try {
							computedAlignment = SequentialComposition.weightedSequentialComposition3(alignmentFiles.get(0), alignmentFiles.get(1), alignmentFiles.get(2));
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
					} else {
						try {
							computedAlignment = SequentialComposition.weightedSequentialComposition4(alignmentFiles.get(0), alignmentFiles.get(1), alignmentFiles.get(2), alignmentFiles.get(3));
						} catch (AlignmentException e1) {
							e1.printStackTrace();
						}
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

					final DefaultCategoryDataset dataset = 
							new DefaultCategoryDataset( );  

					dataset.addValue( precisionValue , "eval" , precision );        
					dataset.addValue( recallValue , "eval" , recall );        
					dataset.addValue( fMeasureValue , "eval" , fMeasure );  

					//add chart
					JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
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
					
					sbMatchingResults.append("\n");
					sbMatchingResults.append("F-measure: " + fMeasureValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Precision: " + precisionValue);
					sbMatchingResults.append("\n");
					sbMatchingResults.append("Recall: " + recallValue);

					matchingResultsPane.setText(sbMatchingResults.toString());
					
				}
				
			}
		});
		btnEvaluate.setToolTipText("");
		btnEvaluate.setBounds(6, 623, 153, 29);
		panelSimpleMatching.add(btnEvaluate);



		//labels
		JLabel lblUploadFiles = new JLabel("Upload files");
		lblUploadFiles.setForeground(new Color(255, 102, 0));
		lblUploadFiles.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblUploadFiles.setBounds(20, 13, 133, 16);
		panelSimpleMatching.add(lblUploadFiles);

		JLabel matchersLabel = new JLabel("Select Matchers");
		matchersLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		matchersLabel.setForeground(new Color(255, 102, 0));
		matchersLabel.setBounds(20, 150, 139, 16);
		panelSimpleMatching.add(matchersLabel);


		JLabel individualProfilesLabel = new JLabel("Ontology Profiles ");
		individualProfilesLabel.setForeground(new Color(255, 102, 0));
		individualProfilesLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		individualProfilesLabel.setBounds(713, 18, 299, 16);
		panelSimpleMatching.add(individualProfilesLabel);

		JLabel matchingResultsLabel = new JLabel("Evaluation Results");
		matchingResultsLabel.setForeground(new Color(255, 102, 0));
		matchingResultsLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		matchingResultsLabel.setBounds(748, 400, 132, 16);
		panelSimpleMatching.add(matchingResultsLabel);

		JLabel lblConfidence = new JLabel("Choose Confidence threshold");
		lblConfidence.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblConfidence.setForeground(new Color(255, 102, 0));
		lblConfidence.setBounds(197, 150, 218, 16);
		panelSimpleMatching.add(lblConfidence);

		JLabel labelSameDomainConstraint = new JLabel("Same domain?");
		labelSameDomainConstraint.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		labelSameDomainConstraint.setToolTipText("The \"Same Domain Constraint\" adds confidence if two concepts from the ontologies being matched are associated with the same WordNet Domain and reduces confidence if they aren´t");
		labelSameDomainConstraint.setForeground(new Color(255, 102, 0));
		labelSameDomainConstraint.setBounds(39, 374, 109, 16);
		panelSimpleMatching.add(labelSameDomainConstraint);

		JLabel lblPriority = new JLabel("Set Priority");
		lblPriority.setForeground(new Color(255, 102, 0));
		lblPriority.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblPriority.setBounds(486, 150, 104, 16);
		panelSimpleMatching.add(lblPriority);

		textFieldCompoundMatcherPriority = new JTextField();
		textFieldCompoundMatcherPriority.setBounds(489, 168, 45, 26);
		panelSimpleMatching.add(textFieldCompoundMatcherPriority);
		textFieldCompoundMatcherPriority.setColumns(10);

		textFieldParentMatcherPriority = new JTextField();
		textFieldParentMatcherPriority.setColumns(10);
		textFieldParentMatcherPriority.setBounds(489, 217, 45, 26);
		panelSimpleMatching.add(textFieldParentMatcherPriority);

		textFieldAncestorMatcherPriority = new JTextField();
		textFieldAncestorMatcherPriority.setColumns(10);
		textFieldAncestorMatcherPriority.setBounds(489, 267, 45, 26);
		panelSimpleMatching.add(textFieldAncestorMatcherPriority);

		textFieldWordNetMatcherPriority = new JTextField();
		textFieldWordNetMatcherPriority.setColumns(10);
		textFieldWordNetMatcherPriority.setBounds(489, 317, 45, 26);
		panelSimpleMatching.add(textFieldWordNetMatcherPriority);

		JLabel lblComposition = new JLabel("Select Composition ");
		lblComposition.setForeground(new Color(255, 102, 0));
		lblComposition.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblComposition.setBounds(20, 435, 395, 16);
		panelSimpleMatching.add(lblComposition);

		

		JLabel lblComputeOntologyProfile = new JLabel("Compute ontology profile");
		lblComputeOntologyProfile.setForeground(new Color(255, 102, 0));
		lblComputeOntologyProfile.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblComputeOntologyProfile.setBounds(318, 13, 194, 16);
		panelSimpleMatching.add(lblComputeOntologyProfile);

		JLabel lblConfigureAutomatically = new JLabel("Automatic matcher configuration");
		lblConfigureAutomatically.setToolTipText("The \"Same Domain Constraint\" adds confidence if two concepts from the ontologies being matched are associated with the same WordNet Domain and reduces confidence if they aren´t");
		lblConfigureAutomatically.setForeground(new Color(255, 102, 0));
		lblConfigureAutomatically.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblConfigureAutomatically.setBounds(352, 74, 182, 16);
		panelSimpleMatching.add(lblConfigureAutomatically);

		JCheckBox checkBoxConfigureMatchersAutomatically = new JCheckBox("");
		checkBoxConfigureMatchersAutomatically.setBounds(320, 70, 37, 23);
		panelSimpleMatching.add(checkBoxConfigureMatchersAutomatically);

		JLabel lblrequiresMin = new JLabel("(Requires min 3 alignments)");
		lblrequiresMin.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblrequiresMin.setForeground(new Color(255, 102, 0));
		lblrequiresMin.setBounds(259, 472, 194, 16);
		panelSimpleMatching.add(lblrequiresMin);

		JLabel label = new JLabel("(Requires min 3 alignments)");
		label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		label.setForeground(new Color(255, 102, 0));
		label.setBounds(259, 503, 194, 16);
		panelSimpleMatching.add(label);

		JLabel label_1 = new JLabel("(Requires min 3 alignments)");
		label_1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		label_1.setForeground(new Color(255, 102, 0));
		label_1.setBounds(259, 537, 194, 16);
		panelSimpleMatching.add(label_1);


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
