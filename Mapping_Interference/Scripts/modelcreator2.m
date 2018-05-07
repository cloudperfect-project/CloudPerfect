storeLocation=inputdlg ({'IP of FTP server', 'Username', 'Pwd:'}, 'Model Repository details', 1, {'192.168.56.101','labuser','userlab2017'})
        ipftp=storeLocation{1};
        modelID='23';
        %FIX retrieve  bia FTP Object dir field
        basepath='/home/labuser/'; %static or retrieved bia FTP Object dir field
        
        %CHECK \  AND / SEEMS TO COPY \23\BOOK1.CSV
        
        F = ftp(storeLocation{1}, storeLocation{2}, storeLocation{3})
        exists='false';
        %FIX once finalized copy to other option for storage without removal of other candidates
        
        %FIX check if contains
        list=dir(F)
        isstr(list(1).name)
        isstr(modelID)
        for i=1:length(list)
          if (strcmp(list(i).name,modelID))&&(list(i).isdir==1)
            exists='true';
            %break;
          endif
        exists
        endfor
        if (strcmp(exists,'true'))
        
          %if yes rename /modelID to /modelID_date
          ftpcom=['rename(F,''',basepath,modelID,''',''',basepath,modelID,'_replaced_',datestr(date),''')']
          eval(ftpcom)
          
        else
          %if not create new
          ftpcom=['mkdir(F,''',basepath,modelID,''')']
          eval(ftpcom)
        endif
        
        
        %end of part for ftp upload details
        %needs a cd into the dir modelID
        %works as  mput(F,'normalize.m') even if quote error is returned, we need to catch the error
        ftpcom=['mput(F,''./',modelID,'/*.*'',''',basepath,modelID,''')']
        eval(ftpcom)
        
        
        finalmsg=["Final model stored in:\n",ipftp]
        h = msgbox (finalmsg);
       
        