# SmellDetectorMerger

SmellDetectorMerger is an Eclipse plug-in which uses a number of code smell detectors (some of which are products of research work) that detect different kinds of code smells. The tool then combines all the detected smells from the different detectors in a single view and displays them to the end user. For each smell the user can see its type, the affected element, as well as a list of names which corresponds to the detectors that spotted this specific smell.

In case the user is interested in getting results from a limited number of detectors, the tool provides this option via a Preference page in Eclipse. Also, the user can select whether they want to detect a specific code smell or all available smells.

### Available detectors
* [CheckStyle](https://github.com/checkstyle/checkstyle)
* [DuDe](https://wettel.github.io/dude.html)
* [PMD](https://github.com/pmd/pmd)
* [JDeodorant](https://github.com/tsantalis/JDeodorant)
* [JSpIRIT](https://github.com/hcvazquez/JSpIRIT)
* [Organic](https://github.com/opus-research/organic)

### Supported code smells
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
