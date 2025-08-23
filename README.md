# bndtools.workspace
Bndtools Remote Service Workspace Template

## NEW (4/28/2025) Bndtools Template for Python.Java Remote Services Development

There has been a new project template added to the [ECF Bndtools Workspace Template](https://github.com/ECF/bndtools.workspace) that uses the [ECF Python.Java Distribution Provider](https://github.com/ECF/Py4j-RemoteServicesProvider).  This distribution provider is based upon py4j, which supports high performance remote procedure call between python and java processes.

To try it out after installing Bndtools 7.1 and the ECF tools add ons

1. Create a new Bndtools Workspace using the [ECF Bndtools Workspace Template](https://github.com/ECF/bndtools.workspace)

![bndtoolsnewwkspace](https://github.com/user-attachments/assets/95ec5792-6bc2-4c88-990d-4e8d3350627e)

2. Create a new Bnd OSGi project

![bndtoolsnewproject](https://github.com/user-attachments/assets/fa2641e6-a074-4796-b761-f79999b9ba06)

3. Open the projectName.hellopython.javahost.bndrun file in the project directory
   
![bndtoolsbndrun](https://github.com/user-attachments/assets/9bf8a380-9ee7-4e48-ac49-1627cf3ace75)

4. Choose 'Resolve' and then 'Update'

5. Select Debug OSGi to start the example application (Java)

![bndtoolsdebug](https://github.com/user-attachments/assets/9fa2536f-9748-4f5f-94bc-b78374f436a8)

Running Python Example Program 

1. Install [iPOPO v 3.1.0](https://ipopo.readthedocs.io) in your Python (3.9 or greater) local environment

2. In a command shell or IDE, navigate to the project directory and run the run_python_example.py script

```
python run_python_example.py
```
The examples will output progress to their respective consoles as the remote services are made exported,
discovered, and imported by the java process or the python process.  

![bndtoolspython](https://github.com/user-attachments/assets/d5bbd4e4-d57c-412a-a198-fe16ed76a95d)

Most of the code that produces output is available in the example project. For java: src/main/java/.../hello/*.java 
and python: python-src/samples/rsa

GRPC Development via Bndtools

This is a video tutorial showing how to use this workspace for gRPC development.  In 4 parts: [Part 1 - API Generation](https://www.youtube.com/watch?v=289BGznS_so), [Part 2 - Remote Service Implementation](https://www.youtube.com/watch?v=58ZU_KIKUAo), [Part 3 - Remote Service Consumer](https://www.youtube.com/watch?v=c4tMPbDPiVw), [Part 4 - Remote Service Debugging](https://www.youtube.com/watch?v=9Q2qfiL8QMA)

