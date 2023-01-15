Running the built TLC tool:
---------------------------------

### Follow the following compilation steps in README.md:


Compiling application classes:

``` shell
ant -f customBuild.xml compile
```

Compiling test classes:

``` shell
ant -f customBuild.xml compile-test
```


### Build the tlatool.jar file:


``` shell
ant -f customBuild.xml dist
```

### Run TLC:

Running TLC to model check a TLA+ file:

``` shell
 java -jar dist/tla2tools.jar  /path-to-TLA-file/MC.tla -config /path-to-config-file/MC.cfg  
```

Running TLC to simulate a given number of executions of a TLA+ file and log the trace in the given file:

``` shell
 java -jar dist/tla2tools.jar -simulate num=1,file=out.txt /path-to-TLA-file/MC.tla -config /path-to-config-file/MC.cfg  
```

For more options, check `TLC.java`.
