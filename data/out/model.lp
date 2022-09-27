\ENCODING=ISO-8859-1
\Problem name: ilog.cplex

Minimize
 obj: z1 + z2 + z3
Subject To
 controllermuscm0_cpu_capacity#0: 10 x4 + 10 x5 + 10 x6 + 10 x7 <= 8000
 controllermuscm0_mem_capacity#1: 53477376 x4 + 53477376 x5 + 53477376 x6
                                 + 53477376 x7 <= 16568090624
 workermeunm0_cpu_capacity#2:    10 x8 + 10 x9 + 10 x10 + 10 x11 <= 7000
 workermeunm0_mem_capacity#3:    53477376 x8 + 53477376 x9 + 53477376 x10
                                 + 53477376 x11 <= 16568090624
 workermnanem0_cpu_capacity#4:   10 x12 + 10 x13 + 10 x14 + 10 x15 <= 7000
 workermnanem0_mem_capacity#5:   53477376 x12 + 53477376 x13 + 53477376 x14
                                 + 53477376 x15 <= 16568090624
 controllermuscm0z1_definition#6: z1 - x4 - x5 - x6 - x7 <= 1
 controllermuscm0z1_definition#7: z1 - 0.01 x4 - 0.01 x5 - 0.01 x6 - 0.01 x7
                                 >= 0.01
 workermeunm0z2_definition#8:    z2 - x8 - x9 - x10 - x11 <= 0
 workermeunm0z2_definition#9:    z2 - 0.01 x8 - 0.01 x9 - 0.01 x10 - 0.01 x11
                                 >= 0
 workermnanem0z3_definition#10:  z3 - x12 - x13 - x14 - x15 <= 0
 workermnanem0z3_definition#11:  z3 - 0.01 x12 - 0.01 x13 - 0.01 x14 - 0.01 x15
                                 >= 0
 pod1_delay_from_controllermuscm0_to_workermuscm2#12: x4 <= 90
 pod1_delay_from_workermeunm0_to_workermuscm2#13: 135 x8 <= 90
 pod1_delay_from_workermnanem0_to_workermuscm2#14: 32 x12 <= 90
 pod1_consistency#15:            x4 + x8 + x12  = 1
 pod2_delay_from_controllermuscm0_to_workermuscm2#16: x5 <= 90
 pod2_delay_from_workermeunm0_to_workermuscm2#17: 135 x9 <= 90
 pod2_delay_from_workermnanem0_to_workermuscm2#18: 32 x13 <= 90
 pod2_consistency#19:            x5 + x9 + x13  = 1
 pod3_delay_from_controllermuscm0_to_workermuscm2#20: x6 <= 90
 pod3_delay_from_workermeunm0_to_workermuscm2#21: 135 x10 <= 90
 pod3_delay_from_workermnanem0_to_workermuscm2#22: 32 x14 <= 90
 pod3_consistency#23:            x6 + x10 + x14  = 1
 pod4_delay_from_controllermuscm0_to_workermuscm2#24: x7 <= 90
 pod4_delay_from_workermeunm0_to_workermuscm2#25: 135 x11 <= 90
 pod4_delay_from_workermnanem0_to_workermuscm2#26: 32 x15 <= 90
 pod4_consistency#27:            x7 + x11 + x15  = 1
Bounds
 0 <= z1 <= 1
 0 <= z2 <= 1
 0 <= z3 <= 1
 0 <= x4 <= 1
 0 <= x5 <= 1
 0 <= x6 <= 1
 0 <= x7 <= 1
 0 <= x8 <= 1
 0 <= x9 <= 1
 0 <= x10 <= 1
 0 <= x11 <= 1
 0 <= x12 <= 1
 0 <= x13 <= 1
 0 <= x14 <= 1
 0 <= x15 <= 1
Binaries
 z1  z2  z3  x4  x5  x6  x7  x8  x9  x10  x11  x12  x13  x14  x15 
End
