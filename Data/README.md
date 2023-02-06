# RQ1

The folder `RQ1` contains the result of the automatic evaluation using different AST mapping algorithms. 

`RQ1` has ten files with project names. Each file is the automatic evaluation of the project.

Each line in the file is formatted as follows: 

- file path + commit url | the number of the AST mapping nodes of iASTMapper | the number of the AST edit actions of iASTMapper | the number of the code edit actions of iASTMapper | the number of the AST mapping nodes of GumTree| the number of the AST edit actions of GumTree| the number of the code edit actions of GumTree | the number of the AST mapping nodes of MTDiff| the number of the AST edit actions of MTDiff| the number of the code edit actions of MTDiff | the number of the AST mapping nodes of IJM| the number of the AST edit actions of IJM| the number of the code edit actions of IJM

For example, a line:

`activemq\activecluster\src\java\org\activecluster\ClusterFactory.java https://github.com/apache/activemq/commit/84077a3663e350e39b4b8a48784e6ed706cc961d | 61 68 7 | 51 69 7 | 51 83 7 | 46 46 7`

It means, the file revision is the file with file path is `activemq\activecluster\src\java\org\activecluster\ClusterFactory.java` and commit url is `https://github.com/apache/activemq/commit/84077a3663e350e39b4b8a48784e6ed706cc961d`.

And the automatic evaluation of this file revision is:

|                   | iASTMapper | GumTree | MTDiff | IJM  |
| :---------------: | :--------: | :-----: | :----: | :--: |
| AST Node Mappings |     61     |   51    |   51   |  46  |
|    AST ES Size    |     68     |   69    |   83   |  46  |
|    AST ES Size    |     7      |    7    |   7    |  7   |

# RQ2

The folder `RQ2` is the evaluation of the RQ2 in paper. It includes two folders `iASTMapper-Inner` and the `iASTMapper-Outer` and a file `result.csv`. 

Each folder has the result evaluated by two participants. They evaluated 50 same file revisions of  `iASTMapper-Inner` and `iASTMapper-Outer`.

The file `RQ2.csv` summarizes the evaluation result of two participants.

# RQ3

The folder `RQ3` contains the result of the manual evaluation using code edit actions. 

`RQ3` has four different groups and each group is evaluated by three participants. The participants in same group share the same 50 file revisions. These four groups have a total of 200 file revisions. 

Each participant will evaluate the result generated from 50 file revisions by AST mapping algorithms (iASTMapper, GumTree, MTDiff and IJM), respectively. 

The folder name of the file revision consists of a file name and commit, such as `AbstractAttributeContainer_295e67201c5583c36ef633503302b3d662f4278b` is the result generated from file called `AbstractAttributeContainer.java`  and the commit is `295e67201c5583c36ef633503302b3d662f4278b`.

The file `RQ3.csv` summarizes the result of the manual evaluation.

# RQ4

The folder `RQ4` contains the result of the runtime of the iASTMapper.

The `RQ4` includes ten files named using the Java project names. Each file conatins the runtime(ms) of each file revvision for the four AST mapping algorithms(iASTMapper, GT, MTD and IJM).

Each line in the file is formatted as follows:

- file path + commit url | the runtime(ms) of the iASTMapper | the runtime(ms) of the GumTree | the runtime(ms) of the MTDiff | the runtime(ms) of the IJM

For example, a line:  `

activemq\activecluster\src\java\org\activecluster\ClusterFactory.java https://github.com/apache/activemq/commit/84077a3663e350e39b4b8a48784e6ed706cc961d | 5 | 11 | 25 | 6

It means, the file revision is the file with file path is `activemq\activecluster\src\java\org\activecluster\ClusterFactory.java` and commit url is `https://github.com/apache/activemq/commit/84077a3663e350e39b4b8a48784e6ed706cc961d` and the runtime of the iASTMapper, GumTre, MTDiff and IJM are 5ms, 11ms, 25ms and 6ms, respectively.
