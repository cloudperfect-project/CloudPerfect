## Copyright (C) 2017 George Kousiouris
## 
## This program is free software; you can redistribute it and/or modify it
## under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 3 of the License, or
## (at your option) any later version.
## 
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU General Public License for more details.
## 
## You should have received a copy of the GNU General Public License
## along with this program.  If not, see <http://www.gnu.org/licenses/>.

## -*- texinfo -*- 
## @deftypefn {Function File} {@var{retval} =} normalize (@var{input1}, @var{input2})
##
## @seealso{}
## @end deftypefn

## Author: George Kousiouris 
## Created: 2017-07-27

function [resp,ps]=normalize(y,a=-1,b=1,ps)

%y is assumed to be rows: number of inputs, columns: concrete data points e.g. 1x105
%if ps has the default value of 0, the min max are extracted from the dataset y 
      if (isstruct(ps))
      
      else
        ps=struct('xmin',[],'xmax', []);
        sizey=size(y)
        for i=1:sizey(1)
          ps.xmin(i)=min(y(i,1:end));
          ps.xmax(i)=max(y(i,1:end));
                  
        end
      endif
      %NORMALIZATION FROM XMIN, XMAX to (a,b)
     
      ps
      sizey=size(y);
      for row_index=1:sizey(1)
        A=ps.xmin(row_index)
        B=ps.xmax(row_index)
        for norm_index2=1:sizey(2)
                resp(row_index,norm_index2)=a+ (y(row_index,norm_index2)-A)*(b-a)/(B-A);   
        end
      end
      save ps.mat ps
      
 endfunction 