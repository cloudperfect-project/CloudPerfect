
function [resp]=createmodel(arg)


namearray=['trainver2_response';'trainver2_stddev';'trainver2_timeout']
  
%FIX NORMALIZE INPUTS, check orientation of columns, seems to be rows: input number, columns: data lines
%old code in normalize_data  script
%equivalent to mapminmax in this script normalization code (from line 211 approximately
loadFileCommand=['load ./',arg,'/config.mat']
eval(loadFileCommand)

%move nnscript to new working dir ./modelID
copyCommand=['copyfile("./nnscript.m","',workDir,'/',modelID,'")'];
eval(copyCommand);

copyCommand=['copyfile("./normalize.m","',workDir,'/',modelID,'")'];
eval(copyCommand);

copyCommand=['copyfile("./denormalize.m","',workDir,'/',modelID,'")'];
eval(copyCommand);

setDirCommand=['cd ./',modelID];
eval(setDirCommand)

%transform to normalize. OUTPUT SHOULD BE TRAINVER2 variable with rows: input number, columns: data lines
%FIX currently hardcoded to load directly trainver2, should be through inputData

inputData=inputData';
[trainver2,ps]=normalize(inputData,-1,1,0);
%loadData=['load ',file];%hardcoded from menu to load /lib/trainver2_response.mat
%eval(loadData)


for namearrayIndex=1:1 %for loop for different types of metrics

  %FIX FIND AND REPLACE LOGIC OF namearrayIndex AND namearray
  
  %load trainver2_time---DELETE
  %loadFileCommand=['load ',namearray(namearrayIndex,1:end)]
  % eval(loadFileCommand)

  
  %needed for eliminating Inf values because of mean absolute value metric
  sizetrain=size(trainver2)
  for zer1=1:sizetrain(1)
      for zer2=1:sizetrain(2)
        if trainver2(zer1,zer2)==0
          trainver2(zer1,zer2)=0.001;
        end
        
      end
  end
        
  
  [train,medval,val]=subset(trainver2,1,1,0.2,0.3) %ηταν 0.2 0.3
  
  sizetrain2=size(train);
  sizemedval2=size(medval);
  sizeval2=size(val);
  num_inputs=str2num(numInputs); 

  for trainsetPercentage=1:0.2:1
    
    save setup namearray namearrayIndex trainsetPercentage
    
    %MAY NEED ESCAPE CHARACTER
    %FIX get from config file and create path
    %path=['./bestnets/',strtrim(namearray(namearrayIndex,1:end)),'_',num2str(trainsetPercentage),'\'];
    
    createdirCommand=['mkdir ./bestnets'];
    eval(createdirCommand)
    
    %sizeval2 does not change, same final validation set 
    sizetrain2(2)=trainsetPercentage*sizetrain2(2)
    sizemedval2(2)=trainsetPercentage*sizemedval2(2)
    
    %STOPPED HERE
    %change path to be loop specific also in NN script
    %save dataforplots and all other files in that path
    
    %extract p,t from train
    p=train(1:num_inputs,1:sizetrain2(2))
    t=train((num_inputs+1):sizetrain2(1),1:sizetrain2(2));

    %extract medvalyin, medvalyout from medval
    medvalyin=medval(1:num_inputs,1:sizemedval2(2));
    medvalyout=medval((num_inputs+1):sizemedval2(1),1:sizemedval2(2));

    %extract sample,valy from val
    sample=val(1:num_inputs,1:sizeval2(2));
    valy=val((num_inputs+1):sizeval2(1),1:sizeval2(2));

    valy=valy';

    namearray(namearrayIndex,1:end)
    %save elearnver2_time.mat p t medvalyin medvalyout sample valy ps
    
    path=['./'];
    %for this we do not care if it has the same name because the files are in different folders
    savecommand=['save ',path,'elearnver2_time.mat p t medvalyin medvalyout sample valy ps']
    %savecommand=['save elearnver2_time.mat p t medvalyin medvalyout sample valy ps']
    eval(savecommand)
    
    %This is going to be called from java
    %Take as arguments: ASC_ID, data file, 

    %An to kanoume me arxeia den tha exei provlhma me pollapla threads? na grafoun sta idia endiamesa arxeia?

    %If it is executed within the prompt--logically this is the case with javaoctave
    %function [exec]=createmodel(asc_id)
    %

    %If it is executed as a batch script
    %arg_list=argv();
    %asc_id=arg_list{1};
    %filename=arg_list{2};


    indicator=1;
    savespergen=0;
    generation=0;
    %save indicator indicator savespergen
    savecommand=['save ',path,'indicator indicator savespergen']
    eval(savecommand)
    
    %save generation generation
    savecommand=['save ',path,'generation generation']
    eval(savecommand)


    %Define NN parameters (number of inputs, number of outputs, range, data file)

    %Pass data set


    %NEW---CREATE VECTORS FROM DATASET
    %load elearnver2_time.mat
    loadcommand=['load ',path,'elearnver2_time.mat']
    eval(loadcommand)
    
    %Set working dir
    %workCommand=['cd ./',modelID];
    %eval(workCommand)
    pwd

    %through workspace?in matlab the two workspaces are separated
    echo off all
    silent_functions
    % Fitness function
    fitnessfcn = @nnscript;
    % Number of Variables
    nvars = 19;
    max_tfs=3;
    % Linear inequality constraints
    A = [];
    b = [];
    % Linear equality constraints
    Aeq = [];
    beq = [];
    % Bounds
    %lb = [3 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1];
    %ub = [10 13 13 13 13 13 13 13 13 13 13 8 8 8 8 8 8 8 8];
    LB=[1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1];
    UB=[10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10];
    %lb=[];
    %ub=[];
    % Nonlinear constraints
    nonlcon = [];
    % Start with default options
    %options = gaoptimset;
    % Modify some parameters

    %POPULATION SIZE IN SOME CASES (=2) CAUSES OCTAVE BUGS ..RHS etc.)
    options = gaoptimset('Generations' ,30,'PopulationSize',20);%3 replaced by max_tfs
    %NORMALIZATION GIA TA YPOLOIPA DIASTHMATA


    %options = gaoptimset(options,'StallTimeLimit' ,Inf);
    %options = gaoptimset(options,'Display' ,'off');
    % Run GA
    %[X,FVAL,REASON,OUTPUT,POPULATION,SCORES] = ga(fitnessFunction,nvars,Aineq,Bineq,Aeq,Beq,LB,UB,nonlconFunction);
    [x,fval,exitflag,output,population,scores] = ga(fitnessfcn,nvars,A,b,Aeq,beq,LB,UB,nonlcon,options);


    %load indicator;
    loadcommand=['load ',path,'indicator']
    eval(loadcommand)
    %path='C:\bestnets\FGCS_MOCS\MappingANN'; %UNFINISHED, based on ASC_ID
    path=['./bestnets'];
    for i=1:(indicator-1)
    eval(['load -binary ',path,'/net',num2str(i)]); 
    end

   
    %load elearnver2_time.mat
    loadcommand=['load elearnver2_time.mat']
    eval(loadcommand)

    %%%%NEW PSYCHAS COMMENT DENORMALIZATION

    %%%%%%PS IS CONTAINED IN elearnver2.mat---SOSOOSOS---beware of the intervals. the normalization was based on rows I think, so we must make sure that the arrays are not
    %%%%%reversed



    %%%%%% We need to convert only the final values (yest and yreal)
    %%%%%END OF NEW PSYCHAS

                
    clear y y2 y1

    max_nets=indicator-1 %TO BE CHANGED EACH TIME  CHANGE
    
    if (max_nets>0)
    
      sample_index=1;
      samplesize=size(valy); %number of validation cases
      inputsize=size(sample);% inputsize(1) is the number of inputs
      max_samples=samplesize(1);
      iter=1;

      for iter=1:max_nets%gia ka8e net---na orizw to megisto me vash ta posa nets vgainoun apo to ga
          for sample_index=1:max_samples%megisto me vash ta deigmata   CHANGE        
          eval(['y(',num2str(sample_index),',',num2str(iter),')=sim(net',num2str(iter),',sample(1:inputsize(1),',num2str(sample_index),'));' ]); %CHANGE?TO 1:4
          sample_index=sample_index+1;   %%to y exei ta apotelesmata tou estimation
          end
          iter=iter+1;
      end


      %PSYCHAS
      % na allaksoume ta y kai valy kai na ta afhsoume unnormalized
      % de mporoume na kanoume apla normalize sto prohgoumeno diasthma giati mporei na mhn yparxoun ta oria tou arxika normalized diasthmatos (-1,1) mesa sto valy

      valy=valy';
      y=y';

      valy=denormalize(valy,ps,-1,1,str2num(numInputs));
      %y=denormalize(y,ps,-1,1,2);
      
       for denormInd=1:max_nets
          %y includes outputs from all saved networks, its a (maxnets x validation size) array
          y(denormInd,1:max_samples)=denormalize(y(denormInd,1:max_samples),ps,-1,1,str2num(numInputs)); 
          denormInd=denormInd+1;
      end
      
      %%%%end of mapminmax


      valy=valy';
      y=y';

      %edw afairw apo ka8e sthlh tou pinaka twn estimations tis pragmatikes times
      %meta tha prepei gia ka8e stoixeio na diairw me to antistoixo twn real gia
      %na vgazw % apokliseis (kai meta pros8eseis klp)
      for j=1:max_nets
          %y1=dy=yest-yreal
          y1(1:max_samples,j)=y(1:max_samples,j)-valy;%----na yparxei hdh to valy  CHANGE NA GINEI MAX SAMPLES
          j=j+1;
      end


                  %HERE HAS THE PROBLEM
                  
          
          
                  
      j=1;
      sample_index=1;
      %extract percentage of error with regards to the real value
      for j=1:max_nets
          sample_index=1;
          % fuzout: estimated from fis output
          % out: real output 

          %RMSE(j) = norm(y(1:end,j) - valy)/sqrt(length(valy));
          %CVRMSE(j) = RMSE./mean(valy);

         for sample_index=1:max_samples       %CHANGE NA GINEI MAX SAMPLES
           y2(sample_index,j)=y1(sample_index,j)/abs(valy(sample_index));  %changed to abs
           sample_index=sample_index+1;
         end
         j=j+1;
        
      end

      %mean error for each ANN model in the validation cases
      mean_error_for_each_model_in_all_validation_cases=mean(abs(y2));

      %reverse y2 in order to have in each column each validation case
      %24 columns, with #rows=to the number of produced models
      y2rev=y2';

      %mean error from combining all produced models for each validation case
      mean_error_from_combination_of_all_models_for_each_validation=mean(y2rev);

      %min_RMSE=min(RMSE)
      %min_CVRMSE=min(CVRMSE)

      final=mean_error_from_combination_of_all_models_for_each_validation';
      overall_mean_error=mean(abs(final));

      %to y2rev einai h vash gia na melethsoume ta windows klp...alla an
      %pairnoume windows pairnoume ta idia montela ka8e fora?h apla ta 10 mesaia
      %px. olwn twn provlepsewn?

      %START OF EXPERIMENTS
      sorted=sort(y2rev);
      median_for_each_validation_case=median(sorted);
      mean_from_medians=mean(abs((median(sorted))'));

      sizes=size(sorted);

      number_of_models=sizes(1);

      half=number_of_models/2;
      %half=round(half)
      window_size=1;
      best_mean_windowed=1000;
      middle1=floor((number_of_models+1)/2);
      middle2=floor((number_of_models+2)/2);
      for window_size=1:middle1
          %window_size=5;
          for column=1:max_samples
             all_cases_windowed_answer(column)=mean(sorted(middle1-window_size+1:middle2+window_size-1,column)); 
          end
          all_cases_windowed_answer;
          overall_mean_windowed=mean(abs((all_cases_windowed_answer)'));
          if overall_mean_windowed<best_mean_windowed
              best_window=window_size;
              best_mean_windowed=overall_mean_windowed;
          end
      end

      
      

      %END OF EXPERIMENTS

      %LINK GIA OUTLIER REMOVAL
      %http://www.mathworks.com/access/helpdesk/help/techdoc/data_analysis/f0-727
      %5.html

      bestmean=min(mean_error_for_each_model_in_all_validation_cases)
      index=1;
      index_of_best=1;
      y2size=size(y2);
      for index=1:y2size(2)
          if bestmean==mean_error_for_each_model_in_all_validation_cases(index)
              index_of_best=index;
              
          end    
      end

      %save dataforplots best_window best_mean_windowed mean_from_medians median_for_each_validation_case overall_mean_error final mean_error_from_combination_of_all_models_for_each_validation mean_error_for_each_model_in_all_validation_cases y2 savespergen bestmean index_of_best valy y 

      savecommand=['save dataforplots best_window best_mean_windowed mean_from_medians median_for_each_validation_case overall_mean_error final mean_error_from_combination_of_all_models_for_each_validation mean_error_for_each_model_in_all_validation_cases y2 savespergen bestmean index_of_best valy y'];
      eval(savecommand);
      
      savecommand2=['save -binary dataforplotsBIN best_window best_mean_windowed mean_from_medians median_for_each_validation_case overall_mean_error final mean_error_from_combination_of_all_models_for_each_validation mean_error_for_each_model_in_all_validation_cases y2 savespergen bestmean index_of_best valy y'];
      eval(savecommand2);
      
      
      subplot(2,2,1);
      plot(mean_error_for_each_model_in_all_validation_cases*100);
      title("Mean Absolute Percentage Error of Models in Validation Set");
      xlabel("Model#");
      ylabel("MAPE %");
      subplot(2,2,2);
      plot(y2(1:end,index_of_best)*100);
      title("Percent Error Per Validation Case of Best Model");
      xlabel("Validation Case");
      ylabel("%Error");
      subplot(2,2,3);
      bar(mean_error_for_each_model_in_all_validation_cases*100);
       xlabel("Model#");
      ylabel("MAPE %");
      disp("Keeping best model...")
      
      copyCommand=['copyfile("./bestnets/net',num2str(index_of_best),'","./")']
      eval(copyCommand);
      
      renameCommand=['rename net',num2str(index_of_best),' bestmodel.mat']
      eval(renameCommand);
      
      load bestmodel.mat
      changeNet=['net=net', num2str(index_of_best),';'];
      eval(changeNet)
      save bestmodel.mat net 
      delete nnscript.m
      delete setup
      delete indicator
      delete generation
      delete normalize.m
      delete denormalize.m
      
      %FIX net name in bestmodel HAS THE INDEX_OF_BEST, NEEDS TO BE AS NET FOR GENERIC PURPOSES IN GETPREDICTION
      
      
      
      %vgazei parapanw diktya giati sthn ousia exei 2 generations, ena to arxiko
      %kai ena to epomeno, pou einai to 1o generation. den exei stadar 30 nets,
      %apla merika den mpainoun kan

     % indicator=1;
     % savespergen=0;
     % generation=0;
     % save indicator indicator savespergen
     % save generation generation
     
     end %end of if no networks have been saved 
    
  end

  
  %save program_end program_end
  
end
resp="Model Created...";
