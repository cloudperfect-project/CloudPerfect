  more off;
  choice = menu ('title', "create single model", "create cascading model", "request prediction")
  
  if (choice<=2)
  %if we go for the cascading model, we need two new fields, with modelIDIn
  %for previous model outputs received as inputs in this one, and  modelIDOut
  %for outputs of this model that feed as inputs to the next model
    if (choice==1)
    reply=inputdlg ({'modelID','numInputs', 'WorkDir', 'InputFile'}, 'Single Model Menu', 1)
    
    %link to load file or csvread and link to preprocess script
    modelID=reply{1};
    numInputs=reply{2};
    workDir=reply{3}
    file=reply{4};
    
    
    %here logic to manipulate inputs
    endif
  
    if (choice==2)
    reply=inputdlg ({'modelID','modelIDIn', 'modelIDOut', 'WorkDir','InputFile'}, 'Cascading Models Menu', 1)
    
    
  
    %parameter from FilenameAndPath needs to be loaded into workspace
    %need to abide by naming convention for variables in file, e.g. dataset
    %or in terms of data format, e.g. csv
    % load or csvread 
    modelID=reply{1};
    modelIDIn=reply{2};
    modelIDOut=reply{3};
    workDir=reply{4}
    file=reply{5};
  
    %by default it will have at least one layer
    layerCount=0;
    do
    %while (newLayer==1)  
      
      layerCount++;
      layerCount
      until (!(yes_or_no("Need another layer?")));
  
      
      endif
    
   endif
   
    if (choice==3)
    %filename and path refer to a multiple request based on inputs 
    reply=inputdlg ({'modelID','Inputs', 'WorkDir','InputFile'}, 'Get Prediction Menu', 1)
    modelID=reply{1};
    Inputs=reply{2};
    workDir=reply{3}
    file=reply{4};
    %here logic to manipulate inputs
    
    k=isempty(reply{2});
    %in order to have a uniform interface, the REST service needs to accept an arrayfun
    % for predictions
    if (k)%if empty
        %use file- put file contents in array argument towards REST service
        %load file and put to arrayArg
      
    else
        %copy Inputs field to the array argument towards the REST service
        arrayArg=Inputs;
    endif
    
    %LIST HERE FOR REST CALLS TO MAPPING SERVICE FOR COMMON INTERFACE
    %https://www.gnu.org/software/octave/doc/v4.0.0/URL-Manipulation.html
    
    
    
    %NEED SOME LOGIC CONVENTION TO STORE RESULT AND POTENTIALLY PLOT??
    
   
    
    endif
    
    path=[workDir,'\',modelID];
    
    createdirCommand=['mkdir ',path]
    eval(createdirCommand) 
    disp('Creating options file....')
    save config.mat modelID modelIDIn modelIDOut workDir file Inputs numInputs
    xmlwrite(modelID)
    disp('Done....Calling model creation...')
    %run("./MappingFGCS_MOCS/Octave/createmodel.m")
    