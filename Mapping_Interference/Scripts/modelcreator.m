    #! C:\Octave\Octave-4.0.3\bin
    more off;
    choice = menu ('Model Creator Tool', "create single model", "create cascading model", "request prediction")
    
  if (choice<=2)
    %if we go for the cascading model, we need two new fields, with modelIDIn
    %for previous model outputs received as inputs in this one, and  modelIDOut
    %for outputs of this model that feed as inputs to the next model
      if (choice==1)
        reply=inputdlg ({'modelID','numInputs', 'WorkDir(Location of model storage)', 'InputFile(Absolute Path)'}, 'Single Model Menu', 1, {'','', 'E:\Projects\CLOUDPERFECT\PROJECT\WP3\Implementation\Mapping','E:\Projects\CLOUDPERFECT\PROJECT\WP3\Implementation\Mapping\lib\inputfile.csv'})
        
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
      reply=inputdlg ({'modelID','Inputs (if no file given)', 'WorkDir','InputFilePath'}, 'Get Prediction Menu', 1,{'','', 'E:\Projects\CLOUDPERFECT\PROJECT\WP3\Implementation\Mapping','E:\Projects\CLOUDPERFECT\PROJECT\WP3\Implementation\Mapping\csvinputs.csv'})
      modelID=reply{1};
      Inputs=reply{2};
      disp(Inputs);
      workDir=reply{3}
      file=reply{4};%includes path
      %here logic to manipulate inputs
      disp("In choice 3....")
      %test
      %modelID='89';
      %workDir='E:\Projects\CLOUDPERFECT\PROJECT\WP3\Implementation\Mapping';
      %file='E:\Projects\CLOUDPERFECT\PROJECT\WP3\Implementation\Mapping\csvinputs.csv';
      
      k=isempty(reply{2});
      workDir=strrep(workDir,'\','/');  
      path=[workDir,'/',modelID];
      %in order to have a uniform interface, the REST service needs to accept an arrayfun
      % for predictions
      if (k)%if empty
          %use file- put file contents in array argument towards REST service
          %load file and put to arrayArg
          workDir=strrep(workDir,'\','/');  
          path=[workDir,'/',modelID];
          %eval(createdirCommand)
          if (!(isempty(file))) %if not empty file        
            file=strrep(file,'\','/');
            disp('Extracting data set....')
      
            arrayArg=csvread(file);
          endif
          
        
      else
      
          %copy Inputs field to the array argument towards the REST service
          arrayArg=Inputs;
          
          
      endif
      
      arrayArg
      %assume that user enters columns as characteristics, rows as values
      %normalize expects the opposite, so we need to change the array dimensions
      arrayArg=arrayArg';
      disp(arrayArg)
      %FIX CHECK IF WE CAN LOAD EVERYTHING THAT HAS A .MAT EXTENSIONS
      loadCom=['load ',path,'/bestmodel.mat']
      eval(loadCom)
      loadCom=['load ',path,'/ps.mat'];
      eval(loadCom)
      loadCom=['load ',path,'/config.mat numInputs']
      eval(loadCom)
      disp('aaaaaaaaaaaaaaaaaaaaa')
      disp(numInputs)
      [normInputs,ps]=normalize(arrayArg,-1,1,ps);  
      %normInputs=normInputs'
      
      for estimindex=1:columns(arrayArg)
      
        disp('norm inputs...')
        normInputs(1:end,estimindex)
        disp('norm outputs...')
        normoutputs(estimindex)=sim(net,normInputs(1:end,estimindex));
        
      end
      arrayArg
      normoutputs
      %denormalize
      %check whether
      %normoutputs=normoutputs'; 
      normoutputs
      ps
      outputs=denormalize(normoutputs,ps,-1,1,str2num(numInputs)) %no offset, we take the overall rows
      outputs=outputs'
      %store
      arrayArg=arrayArg';
      kappa=[arrayArg outputs]
      saveOptCommand=['save ', path,'/estim.mat modelID workDir file outputs arrayArg'];
     % saveOptCommand2=['save ', path,'/estim.csv kappa'];
      eval(saveOptCommand)
     % eval(saveOptCommand2)
      filename = [path "/estim.csv"]
     % csvwrite(filename,kappa);
      
 %-------------------OUTPUT---------------------------------------------------- 
 %-----------------------------------------------------------------------------     
      data = kappa
      header='Input1'
      for i = 2:str2num(numInputs)
          header=[header ; sprintf('%s%d','Input',i)];
      end 
      header=[header ; 'Outputs']     
      %header=['I1';'I1';'O';];    
      [drow dcol] = size (data);
      [hrow hcol] = size (header);

      % open file
      outid = fopen (filename, 'w+');

      % write header
      for idx = 1:hrow
          fprintf (outid, '%s', header(idx,:));
          if idx ~= hrow
             fprintf (outid, '                 ');
          else
             fprintf (outid, '\n' );
          end
          if idx == hrow-1
            fprintf (outid, '   ');
          end    
      end

      fclose(outid);


      csvwrite(filename,kappa,'-append');
      
 %-----------------------------------------------------------------------------

 
      pause
      %plot?
      
      
      %LIST HERE FOR REST CALLS TO MAPPING SERVICE FOR COMMON INTERFACE
      %https://www.gnu.org/software/octave/doc/v4.0.0/URL-Manipulation.html
      
      
      
      %NEED SOME LOGIC CONVENTION TO STORE RESULT AND POTENTIALLY PLOT??
      
     
      break
    endif
    
  
if (choice==3)
  
    break
    
    endif
    
    btn2 = questdlg ("Is the output of ascending (higher is better) or descending order?", "Output Order", "Yes, it is ascending (e.g. throughput)", "No, it is descending (e.g. response time)","Yes, it is ascending (e.g. throughput)")
    if (strcmp(btn2,"Yes, it is ascending (e.g. throughput)")==1)
      ascending=true;
    elseif
      ascending=false;
    endif
      
    btn3 = questdlg ("Do you prefer over or under provisioning?", "Provisioning preference", "Over, I focus on my customer needs", "Under, I want to maximize profit","Over, I focus on my customer needs")
    if (strcmp(btn3,"Over, I focus on my customer needs")==1)
      overprovisioning=true;
    elseif
      overprovisioning=false;
    endif
      
    workDir=strrep(workDir,'\','/');
    
    
    path=[workDir,'/',modelID];
    
    createdirCommand=['mkdir ',path]
    eval(createdirCommand)
   
    if (!(isempty(file))) %if not empty file
     
    
      file=strrep(file,'\','/');
      disp('Extracting data set....')
    
      inputData=csvread(file);
      if (choice==1)
        numOutputs=num2str(size(inputData,2)-str2num(numInputs));%for consistency with numOutputs, both to be strings
      endif
    elseif (choice==3)
        inputData=Inputs;      
      
    endif
    
    disp('Done...Creating config or estim file....')
    
    if (choice==1)
      saveOptCommand=['save ', path,'/config.mat modelID modelIDIn modelIDOut workDir file Inputs numInputs numOutputs inputData ascending overprovisioning'];
      eval(saveOptCommand)
         
      disp('Done....Calling model creation...')
      runCommand=["resp=createmodel(\"",modelID,'")']
      eval(runCommand)
      %run("./lib/createmodel.m "+modelID)
      
      %needs to be here, since in this script we know the check box selection (for keeping all models) and can manipulate directory structure
      btn = questdlg ("Are you satisfied with the model performance?", "Model Selection Confirmation based on your preferences", "Yes, store it and remove other candidates", "Yes, but store other candidates", "No, remove all models","Yes, store it and remove other candidates" )
      %FIX: logic to delete or store the models plus logic to move all non selected models
      
      changeCommand=['cd ',workDir];
      eval(changeCommand);
      
      path=[workDir,'/',modelID];
      confirm_recursive_rmdir(false,"local")
      if (strcmp(btn,"Yes, store it and remove other candidates")==1)
       rmCommand=['rmdir ("',path,'/bestnets","s")'];
        eval(rmCommand)
        
        %part for ftp upload details
        storeLocation=inputdlg ({'IP of FTP server', 'Username', 'Pwd:'}, 'Model Repository details', 1, {'192.168.1.1','anonymous',''});
        ipftp=storeLocation{1};
        
        #{
        %check if exists and rename /modelID dir?to keep record of the old models just in case
        F = ftp(storeLocation{1}, storeLocation{2}, storeLocation{3});
        dir(F);
        %FIX check options sequence and format for ftp commands
            %now it assumes local target-> destination for mput
        %FIX once finalized copy to other option for storage without removal of other candidates
        %check if contains /modelID
        ftpcom=['rename(F,''/',modelID,''',''/',modelID,'_replaced_',datestr(date),''')'];
        eval(ftpcom);
        %create new
        ftpcom=['mkdir(F,''/',modelID,''')'];
        eval(ftpcom);
        %end of part for ftp upload details
        ftpcom=['mput(F,''/',modelID,'/*'',''/',modelID,''')'];
        eval(ftpcom);
        #}
        
        finalmsg=["Final model stored in:\n",ipftp]
        h = msgbox (finalmsg);
      endif
          
      if (strcmp(btn,"No, remove all models")==1)
        rmCommand=['rmdir ("',path,'","s")'];
        eval(rmCommand)
      endif
      
      if (strcmp(btn,"Yes, but store other candidates")==1)
        finalmsg=["Final model stored in:\n",path]
        h = msgbox (finalmsg);
      endif
    
    elseif (choice==3)
      saveOptCommand=['save ', path,'/estim.mat modelID modelIDIn modelIDOut workDir file Inputs numInputs numOutputs inputData ascending overprovisioning'];
      eval(saveOptCommand)
    
    endif
     
     
   