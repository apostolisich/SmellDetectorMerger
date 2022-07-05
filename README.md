# SmellDetectorMerger

SmellDetectorMerger is an Eclipse plugin which integrates a number of code smell detectors, some of which are products of research work. It provides aggregate detection results (from the different detectors) which are displayed to the end user inside a View in a table format. For each detected smell, the user can see its type, the affected element, as well as a list of names which correspond to the detectors that found it.

# Features
* Filtering of the results in order to only keep those that were detected by >=2 or >50% of the tools
* Import/export of the results from/to a csv file
* Calculate accuracy and precision of the detectors based on internally created gold standard sets
* Preference page which allows the user to select the detectors that will be used during execution

# Available detectors
* [CheckStyle](https://github.com/checkstyle/checkstyle)
* [DuDe](https://wettel.github.io/dude.html)
* [PMD](https://github.com/pmd/pmd)
* [JDeodorant](https://github.com/tsantalis/JDeodorant)
* [JSpIRIT](https://github.com/hcvazquez/JSpIRIT)
* [Organic](https://github.com/opus-research/organic)

# Supported code smells
* God Class _(CheckStyle, PMD, JDeodorant, JSpIRIT, Organic)_
* Long Method _(CheckStyle, PMD, JDeodorant, JSpIRIT, Organic)_
* Long Parameter List _(CheckStyle, PMD, Organic)_
* Feature Envy _(JDeodorant, JSpIRIT, Organic)_
* Duplicate Code _(DuDe, PMD)_
* Brain Class _(JSpIRIT, Organic)_
* Brain Method _(JSpIRIT, Organic)_
* Data Class _(JSpIRIT, Organic)_
* Dispersed Coupling _(JSpIRIT, Organic)_
* Intensive Coupling _(JSpIRIT, Organic)_
* Refused Parent Bequest _(JSpIRIT, Organic)_
* Shotgun Surgery _(JSpIRIT, Organic)_
* Tradition Breaker _(JSpIRIT)_
* Type Checking _(JDeodorant)_
* Class Data Should Be Private _(Organic)_
* Complex Class _(Organic)_
* Lazy Class _(Organic)_
* Message Chain _(Organic)_
* Speculative Generality _(Organic)_
* Spaghetti Code _(Organic)_

# Installation
In order to install the tool in Eclipse, follow the steps below:
1. Download the jar file of the tool from the latest release [here](https://github.com/apostolisich/SmellDetectorMerger/releases)
2. Close Eclipse (in case there is an instance open)
3. Copy the downloaded jar file and paste it in the _dropins_ folder (it is located in the installation directory of Eclipse)
4. Open Eclipse and select _File -> Restart_ from the menu to make sure it's refreshed

After completing the previous steps, the tool can run by right-clicking the root folder of the desired project and selecting _SmellDetectorMerger_ followed by the desired smell to be detected.

# TechDebt 2022
A paper was written to showcase the plugin, comparing the detection results of the underlying tools. This was accepted, among other papers, for presentation in the [TechDebt 2022 conference](https://2022.techdebtconf.org/details/TechDebt-2022-tools-track/1/Merging-Smell-Detectors-Evidence-on-the-Agreement-of-Multiple-Tools), an international conference related to Technical Debt. The paper can be found [here](https://ieeexplore.ieee.org/document/9804506).
