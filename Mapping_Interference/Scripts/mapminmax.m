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
## @deftypefn {Function File} {@var{retval} =} mapminmax (@var{input1}, @var{input2})
##
## @seealso{}
## @end deftypefn

## Author: George Kousiouris 
## Created: 2017-07-27

function [resp,ps] = mapminmax (mode,y,a=-1,b=1,ps=0,psoffset=0)

  if (strcmp(mode,'apply')==1)
    [resp,ps]=normalize(y,a,b,ps);
  elseif (strcmp(mode,'reverse')==1)
    resp=denormalize(y,ps,a,b,psoffset)
  
  endif
endfunction


