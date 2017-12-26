function [r] = ffsigmoid (x)
  r = 1 ./ (1 + e.^(-x));
endfunction
