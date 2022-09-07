# Guide for running the code

# iASTMapper:

An Iterative Similarity-Based Abstract Syntax Tree Mapping Algorithm

# How to run our code
1. Clone the ten studied projects (ActiveMQ and so on) to the directory `D://tmp`. You can change the path in `resources/config.properties`.
2. Run `/test/java/unit/baseline/BaselineTest`. This is the test for the baseline and the iASTMapper algorithms.
```
// To test the baseline. 
// Optional methods (gt,mtd,ijm)
TestUtils.testBaseline(project, commitId, file, "gt");

// To test iASTMapper
TestUtils.testiASTMapper(project, commitId, file);
```

3. Run `cs.zju.analysis.CommitAnalysis`. The `CommitAnalysis` includes a lot of the APIs using `jgit` and quickly extract the content of each file before and after a commit.
```
// run analysis using iASTMapper for all the file revisions of a commit.
CommitAnalysis analysis = new CommitAnalysis(project, commitId);

// run analysis for each file revision using iASTMapper
analysis.calResultMappings(false, false);

// RevisionAnalysis provides APIs to retrieve the mappings of elements, 
// mappings of tree nodes, edit actions and so on
Map<String, RevisionAnalysis> resultMap = analysis.getRevisionAnalysisResultMap();

// Get AST edit actions and Code edit actions 
for (String filePath: resultMap) {
    List<TreeEditAction>  actions    = generateEditActions();
    List<StmtTokenAction> actionList = generateActions();
    ...
}
```
