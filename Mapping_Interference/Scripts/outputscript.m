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
             fprintf (outid, ' ');
          else
             fprintf (outid, '\n' );
          end
          if idx == hrow-1
            fprintf (outid, '  ');
          end    
      end

      fclose(outid);


      dlmwrite (filename, data,'delimiter','\t  \t \t', '-append' );