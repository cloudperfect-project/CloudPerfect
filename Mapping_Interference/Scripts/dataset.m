%{
filename='C:\Users\ntua\Desktop\datasetsorted.csv'
filename2='C:\Users\ntua\Documents\Mapping\dataset.csv';
I=fopen(filename)
i=0;
while (l = fgetl (I)) != -1
  i=i+1;
  a = strread (l, "%s");
  if (length(strsplit(a{1},","))<10)
      disp(i)
  endif    
endwhile

A=csvread(filename)
first=A(:,7)
second=A(:,8)
third=A(:,10)
B=[first second third]
csvwrite(filename,B)
%}

filename2='C:\Users\ntua\Documents\Mapping\inputfile.csv'
A=csvread(filename2);
[train,medval,val]=subset(A',1,1,0.2,0.2);
val
