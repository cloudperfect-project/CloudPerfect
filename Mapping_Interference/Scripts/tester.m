      
     
      path='C:/Users/ntua/Documents/Mapping'
      saveOptCommand=['save ', path,'/tester.csv'];
      eval(saveOptCommand)
     % eval(saveOptCommand2)
      filename = [path "/estim.csv"]
      A=[1,2,3]
      A=A'
      B=[1,2,3]
      B=B'
      C=[A B]
      D=[1,2,3]
      D=D'
      C=[A B D]
      %csvwrite(filename,kappa);
    %  dlmwrite(filename,'The prediction bruh','')
     % dlmwrite(filename,C,',')
      %dlmwrite(filename,D,'   ,','-append')
      
data = C
header='Input1'
for i = 2:2
      header=[header ; sprintf('%s%d','      Input',i)];
end 
disp(header)
header=[header ; 'Predictions']     
%header=['I1';'I1';'O';];    
[drow dcol] = size (data);
[hrow hcol] = size (header);

% open file
outid = fopen (filename, 'w+');

% write header
for idx = 1:hrow
    fprintf (outid, '%s', header(idx,:));
    if idx ~= hrow
        fprintf (outid, '');
    else
        fprintf (outid, '\n' );
    end
end
% close file
fclose(outid);

% write data
dlmwrite (filename,'           ','', '-append' );
dlmwrite (filename, data,' , ', '-append' );
 
      %dlmwrite(filename,'      ','','-append'); 
      