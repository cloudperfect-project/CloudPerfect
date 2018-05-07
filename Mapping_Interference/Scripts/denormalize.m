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
## @deftypefn {Function File} {@var{retval} =} denormalize (@var{input1}, @var{input2})
##
## @seealso{}
## @end deftypefn

## Author: George Kousiouris 
## Created: 2017-07-27

function [resp]=denormalize(y,ps,a=-1,b=1,psoffset)
%ps to include xmin, xmax arrays per row
%psoffset is a way to denormalize based on an offset from the original ps
%e.g. in cases when we need only the output column to be denormalized

%y is assumed to be rows: number of inputs,  columns: concrete data points e.g. 1x105
      
      %DENORMALIZATION from (a,b) to (A,B) included in ps
      
            
      
      sizey=size(y)
      row_index=1;
      
      for row_index=1:sizey(1)
        
        A=ps.xmin(row_index+psoffset);
        B=ps.xmax(row_index+psoffset);
        norm_index2=1;
        for norm_index2=1:sizey(2)
                resp(row_index,norm_index2)=(A-B)*y(row_index,norm_index2)/(a-b)+(a*B-b*A)/(a-b);
        end
      end
      
      
 endfunction %no need to return the values explicitely