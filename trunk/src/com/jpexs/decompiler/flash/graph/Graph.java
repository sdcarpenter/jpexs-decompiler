/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.graph;

import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class Graph {

   public List<GraphPart> heads;
   private GraphSource code;

   public Graph(GraphSource code, List<Integer> alternateEntries) {
      heads = makeGraph(code, new ArrayList<GraphPart>(), alternateEntries);
      this.code = code;
      for (GraphPart head : heads) {
         fixGraph(head);
         makeMulti(head, new ArrayList<GraphPart>());
      }

   }

   protected static void populateParts(GraphPart part, List<GraphPart> allParts) {
      if (allParts.contains(part)) {
         return;
      }
      allParts.add(part);
      for (GraphPart p : part.nextParts) {
         populateParts(p, allParts);
      }
   }

   private void fixGraph(GraphPart part) {
      while (fixGraphOnce(part, new ArrayList<GraphPart>(), false)) {
      }
   }

   private boolean fixGraphOnce(GraphPart part, List<GraphPart> visited, boolean doChildren) {
      if (visited.contains(part)) {
         return false;
      }
      visited.add(part);
      boolean fixed = false;
      int i = 1;
      String lastpref = null;
      boolean modify = true;
      int prvni = -1;

      if (!doChildren) {

         List<GraphPart> uniqueRefs = new ArrayList<GraphPart>();
         for (GraphPart r : part.refs) {
            if (!uniqueRefs.contains(r)) {
               uniqueRefs.add(r);
            }
         }
         loopi:
         for (; i <= part.path.length(); i++) {
            lastpref = null;
            int pos = -1;
            for (GraphPart r : uniqueRefs) {
               pos++;
               if (r.path.startsWith("e")) {
                  continue;
               }
               if (part.leadsTo(r, new ArrayList<GraphPart>())) {
                  //modify=false;
                  //continue;
               }

               prvni = pos;
               if (i > r.path.length()) {
                  i--;
                  break loopi;
               }
               if (lastpref == null) {
                  lastpref = r.path.substring(0, i);
               } else {
                  if (!r.path.startsWith(lastpref)) {
                     i--;
                     break loopi;
                  }
               }
            }
         }
         if (i > part.path.length()) {
            i = part.path.length();
         }
         if (modify && ((uniqueRefs.size() > 1) && (prvni >= 0))) {
            String newpath = uniqueRefs.get(prvni).path.substring(0, i);
            if (!part.path.equals(newpath)) {
               if (part.path.startsWith(newpath)) {
                  String origPath = part.path;
                  GraphPart p = part;
                  part.path = newpath;
                  while (p.nextParts.size() == 1) {
                     p = p.nextParts.get(0);
                     if (!p.path.equals(origPath)) {
                        break;
                     }
                     p.path = newpath;
                  }
                  fixGraphOnce(part, new ArrayList<GraphPart>(), true);
                  fixed = true;
               }
            }
         }
      } else {

         if (!fixed) {
            if (part.nextParts.size() == 1) {
               if (!(part.path.startsWith("e") && (!part.nextParts.get(0).path.startsWith("e")))) {
                  if (part.nextParts.get(0).path.length() > part.path.length()) {
                     part.nextParts.get(0).path = part.path;
                     fixed = true;
                  }
               }
            }
            if (part.nextParts.size() > 1) {
               for (int j = 0; j < part.nextParts.size(); j++) {
                  GraphPart npart = part.nextParts.get(j);

                  if (npart.path.length() > part.path.length() + 1) {
                     npart.path = part.path + "" + j;
                     fixed = true;
                  }
               }
            }
         }

      }
      for (GraphPart p : part.nextParts) {
         fixGraphOnce(p, visited, doChildren);
      }
      return fixed;
   }

   private void makeMulti(GraphPart part, List<GraphPart> visited) {
      if (visited.contains(part)) {
         return;
      }
      visited.add(part);
      GraphPart p = part;
      List<GraphPart> multiList = new ArrayList<GraphPart>();
      multiList.add(p);
      while ((p.nextParts.size() == 1) && (p.nextParts.get(0).refs.size() == 1)) {
         p = p.nextParts.get(0);
         multiList.add(p);
      }
      if (multiList.size() > 1) {
         GraphPartMulti gpm = new GraphPartMulti(multiList);
         gpm.refs = part.refs;
         GraphPart lastPart = multiList.get(multiList.size() - 1);
         gpm.nextParts = lastPart.nextParts;
         for (GraphPart next : gpm.nextParts) {
            int index = next.refs.indexOf(lastPart);
            if (index == -1) {

               continue;
            }
            next.refs.remove(lastPart);
            next.refs.add(index, gpm);
         }
         for (GraphPart parent : part.refs) {
            if (parent.start == -1) {
               continue;
            }
            int index = parent.nextParts.indexOf(part);
            if (index == -1) {
               continue;
            }
            parent.nextParts.remove(part);
            parent.nextParts.add(index, gpm);
         }
      }
      for (int i = 0; i < part.nextParts.size(); i++) {
         makeMulti(part.nextParts.get(i), visited);
      }
   }

   public GraphPart deepCopy(GraphPart part, List<GraphPart> visited, List<GraphPart> copies) {
      if (visited == null) {
         visited = new ArrayList<GraphPart>();
      }
      if (copies == null) {
         copies = new ArrayList<GraphPart>();
      }
      if (visited.contains(part)) {
         return copies.get(visited.indexOf(part));
      }
      visited.add(part);
      GraphPart copy = new GraphPart(part.start, part.end);
      copy.path = part.path;
      copies.add(copy);
      copy.nextParts = new ArrayList<GraphPart>();
      for (int i = 0; i < part.nextParts.size(); i++) {
         copy.nextParts.add(deepCopy(part.nextParts.get(i), visited, copies));
      }
      for (int i = 0; i < part.refs.size(); i++) {
         copy.refs.add(deepCopy(part.refs.get(i), visited, copies));
      }
      return copy;
   }

   public void resetGraph(GraphPart part, List<GraphPart> visited) {
      if (visited.contains(part)) {
         return;
      }
      visited.add(part);
      int pos = 0;
      for (GraphPart p : part.nextParts) {
         if (!visited.contains(p)) {
            p.path = part.path + pos;
         }
         resetGraph(p, visited);
         pos++;
      }
   }

   public GraphPart getCommonPart(List<GraphPart> parts) {
      GraphPart head = new GraphPart(0, 0);
      head.nextParts.addAll(parts);
      List<GraphPart> allVisited = new ArrayList<GraphPart>();
      head = deepCopy(head, allVisited, null);
      for (GraphPart g : head.nextParts) {
         for (GraphPart r : g.refs) {
            r.nextParts.remove(g);
         }
         g.refs.clear();
         g.refs.add(head);
      }
      head.path = "0";
      resetGraph(head, new ArrayList<GraphPart>());
      fixGraph(head);

      /*Graph gr=new Graph();
       gr.heads=new ArrayList<GraphPart>();
       gr.heads.add(head);
       GraphFrame gf=new GraphFrame(gr, "");
       gf.setVisible(true);
       */

      GraphPart next = head.getNextPartPath(new ArrayList<GraphPart>());
      if (next == null) {
         return null;
      }
      for (GraphPart g : allVisited) {
         if (g.start == next.start) {
            return g;
         }
      }
      return null;
   }

   public GraphPart getNextNoJump(GraphPart part) {
      while (code.get(part.start).isJump()) {
         part = part.getSubParts().get(0).nextParts.get(0);
      }
      return part;
   }

   public static List<GraphTargetItem> translateViaGraph(List localData, String path, GraphSource code, List<Integer> alternateEntries) {
      Graph g = new Graph(code, alternateEntries);
      return g.translate(localData);
   }

   public List<GraphTargetItem> translate(List localData) {
      List<GraphPart> allParts = new ArrayList<GraphPart>();
      for (GraphPart head : heads) {
         populateParts(head, allParts);
      }
      List<GraphTargetItem> ret = printGraph(localData, new Stack<GraphTargetItem>(), allParts, null, heads.get(0), null, new ArrayList<Loop>(), new HashMap<Loop, List<GraphTargetItem>>());
      finalProcessAll(ret,0);
      return ret;
   }

   private void finalProcessAll(List<GraphTargetItem> list,int level) {
      finalProcess(list,level);
      for (GraphTargetItem item : list) {
         if (item instanceof Block) {
            List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
            for (List<GraphTargetItem> sub : subs) {
               finalProcessAll(sub,level+1);
            }
         }
      }
   }

   protected void finalProcess(List<GraphTargetItem> list,int level) {
   }

   protected List<GraphPart> getLoopsContinues(List<Loop> loops) {
      List<GraphPart> ret = new ArrayList<GraphPart>();
      for (Loop l : loops) {
         if (l.loopContinue != null) {
            ret.add(l.loopContinue);
         }
      }
      return ret;
   }

   protected GraphTargetItem checkLoop(GraphPart part, GraphPart stopPart, List<Loop> loops) {
      if (part == stopPart) {
         return null;
      }
      for (Loop l : loops) {
         if (l.loopContinue == part) {
            return (new ContinueItem(null, l.id));
         }
         if (l.loopBreak == part) {
            return (new BreakItem(null, l.id));
         }
      }
      return null;
   }

   private void checkContinueAtTheEnd(List<GraphTargetItem> commands, Loop loop) {
      if (!commands.isEmpty()) {
         if (commands.get(commands.size() - 1) instanceof ContinueItem) {
            if (((ContinueItem) commands.get(commands.size() - 1)).loopId == loop.id) {
               commands.remove(commands.size() - 1);
            }
         }
      }
   }

   protected List<GraphTargetItem> check(List localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, List<GraphTargetItem> output, HashMap<Loop, List<GraphTargetItem>> forFinalCommands) {
      return null;
   }

   protected GraphPart checkPart(List localData, GraphPart part) {
      return part;
   }

   protected GraphTargetItem translatePartGetStack(List localData, GraphPart part, Stack<GraphTargetItem> stack) {
      stack = (Stack<GraphTargetItem>) stack.clone();
      translatePart(localData, part, stack);
      return stack.pop();
   }

   protected List<GraphTargetItem> translatePart(List localData, GraphPart part, Stack<GraphTargetItem> stack) {
      List<GraphPart> sub = part.getSubParts();
      List<GraphTargetItem> ret = new ArrayList<GraphTargetItem>();
      int end = 0;
      for (GraphPart p : sub) {
         if (p.end == -1) {
            p.end = code.size() - 1;
         }
         if (p.start == code.size()) {
            continue;
         } else if (p.end == code.size()) {
            p.end--;
         }
         end = p.end;
         int start = p.start;
         ret.addAll(code.translatePart(localData, stack, start, end));
      }
      return ret;
   }

   protected List<GraphTargetItem> printGraph(List localData, Stack<GraphTargetItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, HashMap<Loop, List<GraphTargetItem>> forFinalCommands) {
      //String methodPath, Stack<GraphTargetItem> stack, Stack<TreeItem> scopeStack, List<GraphPart> allParts, List<ABCException> parsedExceptions, List<Integer> finallyJumps, int level, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, HashMap<Integer, TreeItem> localRegs, MethodBody body, List<Integer> ignoredSwitches
      List<GraphTargetItem> ret = new ArrayList<GraphTargetItem>();
      boolean debugMode = false;


      if (debugMode) {
         System.err.println("PART " + part);
      }

      if (part == stopPart) {
         return ret;
      }
      if (part == null) {
         //return ret;
      }
      part = checkPart(localData, part);
      if (part == null) {
         return ret;
      }

      if (part.ignored) {
         return ret;
      }
      List<String> fqn = new ArrayList<String>();
      //HashMap<Integer, String> lrn = new HashMap<Integer, String>();
      List<GraphTargetItem> output = new ArrayList<GraphTargetItem>();
      //boolean isSwitch = false;

      // code.initToSource();
      List<GraphPart> parts = new ArrayList<GraphPart>();
      if (part instanceof GraphPartMulti) {
         parts = ((GraphPartMulti) part).parts;
      } else {
         parts.add(part);
      }
      boolean isIf = false;
      int end = part.end;
      for (GraphPart p : parts) {
         end = p.end;
         int start = p.start;
         isIf = false;
         /*if (code.get(end).isBranch()) {
          end--;
          }*/

         output.addAll(code.translatePart(localData, stack, start, end));

      }
      if (part.nextParts.size() == 2) {


         if ((stack.size() >= 2) && (stack.get(stack.size() - 1) instanceof NotItem) && (((NotItem) (stack.get(stack.size() - 1))).getOriginal() == stack.get(stack.size() - 2))) {
            ret.addAll(output);
            GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
            GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
            boolean reversed = false;
            List<GraphPart> loopContinues = getLoopsContinues(loops);
            loopContinues.add(part);
            if (sp1.leadsTo(sp0, loopContinues)) {
            } else if (sp0.leadsTo(sp1, loopContinues)) {
               reversed = true;
            }
            GraphPart next = reversed ? sp0 : sp1;
            GraphTargetItem ti;
            if ((ti = checkLoop(next, stopPart, loops)) != null) {
               ret.add(ti);
            } else {
               printGraph(localData, stack, allParts, parent, next, reversed ? sp1 : sp0, loops, forFinalCommands);
               GraphTargetItem second = stack.pop();
               GraphTargetItem first = stack.pop();
               if (!reversed) {
                  AndItem a = new AndItem(null, first, second);
                  stack.push(a);
                  a.firstPart = part;
                  if (second instanceof AndItem) {
                     a.firstPart = ((AndItem) second).firstPart;
                  }
                  if (second instanceof OrItem) {
                     a.firstPart = ((AndItem) second).firstPart;
                  }
               } else {
                  OrItem o = new OrItem(null, first, second);
                  stack.push(o);
                  o.firstPart = part;
                  if (second instanceof OrItem) {
                     o.firstPart = ((OrItem) second).firstPart;
                  }
                  if (second instanceof OrItem) {
                     o.firstPart = ((OrItem) second).firstPart;
                  }
               }
               next = reversed ? sp1 : sp0;
               if ((ti = checkLoop(next, stopPart, loops)) != null) {
                  ret.add(ti);
               } else {
                  ret.addAll(printGraph(localData, stack, allParts, parent, next, stopPart, loops, forFinalCommands));
               }
            }
            return ret;
         } else if ((stack.size() >= 2) && (stack.get(stack.size() - 1) == stack.get(stack.size() - 2))) {
            ret.addAll(output);
            GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
            GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
            boolean reversed = false;
            List<GraphPart> loopContinues = getLoopsContinues(loops);
            loopContinues.add(part);
            if (sp1.leadsTo(sp0, loopContinues)) {
            } else if (sp0.leadsTo(sp1, loopContinues)) {
               reversed = true;
            }
            GraphPart next = reversed ? sp0 : sp1;
            GraphTargetItem ti;
            if ((ti = checkLoop(next, stopPart, loops)) != null) {
               ret.add(ti);
            } else {
               printGraph(localData, stack, allParts, parent, next, reversed ? sp1 : sp0, loops, forFinalCommands);
               GraphTargetItem second = stack.pop();
               GraphTargetItem first = stack.pop();
               if (reversed) {
                  AndItem a = new AndItem(null, first, second);
                  stack.push(a);
                  a.firstPart = part;
                  if (second instanceof AndItem) {
                     a.firstPart = ((AndItem) second).firstPart;
                  }
                  if (second instanceof OrItem) {
                     a.firstPart = ((AndItem) second).firstPart;
                  }
               } else {
                  OrItem o = new OrItem(null, first, second);
                  stack.push(o);
                  o.firstPart = part;
                  if (second instanceof OrItem) {
                     o.firstPart = ((OrItem) second).firstPart;
                  }
                  if (second instanceof OrItem) {
                     o.firstPart = ((OrItem) second).firstPart;
                  }
               }

               next = reversed ? sp1 : sp0;
               if ((ti = checkLoop(next, stopPart, loops)) != null) {
                  ret.add(ti);
               } else {
                  ret.addAll(printGraph(localData, stack, allParts, parent, next, stopPart, loops, forFinalCommands));
               }
            }

            return ret;
         }   /*if ((((ins.definition instanceof IfStrictNeIns)) && ((part.nextParts.get(1).getHeight() == 2) && (code.code.get(part.nextParts.get(1).start).definition instanceof PushByteIns) && (code.code.get(part.nextParts.get(1).nextParts.get(0).end).definition instanceof LookupSwitchIns)))
          || (((ins.definition instanceof IfStrictEqIns)) && ((part.nextParts.get(0).getHeight() == 2) && (code.code.get(part.nextParts.get(0).start).definition instanceof PushByteIns) && (code.code.get(part.nextParts.get(0).nextParts.get(0).end).definition instanceof LookupSwitchIns)))) {
          ret.addAll(output);
          boolean reversed = false;
          if (ins.definition instanceof IfStrictEqIns) {
          reversed = true;
          }
          TreeItem switchedObject = null;
          if (!output.isEmpty()) {
          if (output.get(output.size() - 1) instanceof SetLocalTreeItem) {
          switchedObject = ((SetLocalTreeItem) output.get(output.size() - 1)).value;
          }
          }
          if (switchedObject == null) {
          switchedObject = new NullTreeItem(null);
          }
          HashMap<Integer, TreeItem> caseValuesMap = new HashMap<Integer, TreeItem>();

          stack.pop();
          caseValuesMap.put(code.code.get(part.nextParts.get(reversed ? 0 : 1).start).operands[0], stack.pop());

          GraphPart switchLoc = part.nextParts.get(reversed ? 0 : 1).nextParts.get(0);


          while ((code.code.get(part.nextParts.get(reversed ? 1 : 0).end).definition instanceof IfStrictNeIns)
          || (code.code.get(part.nextParts.get(reversed ? 1 : 0).end).definition instanceof IfStrictEqIns)) {
          part = part.nextParts.get(reversed ? 1 : 0);
          List<GraphPart> ps = part.getSubParts();
          for (GraphPart p : ps) {
          code.toSourceOutput(false, false, 0, localRegs, stack, scopeStack, abc, abc.constants, abc.method_info, body, p.start, p.end - 1, lrn, fqn, new boolean[code.code.size()]);
          }
          stack.pop();
          if (code.code.get(part.end).definition instanceof IfStrictNeIns) {
          reversed = false;
          } else {
          reversed = true;
          }
          caseValuesMap.put(code.code.get(part.nextParts.get(reversed ? 0 : 1).start).operands[0], stack.pop());

          }
          boolean hasDefault = false;
          GraphPart dp = part.nextParts.get(reversed ? 1 : 0);
          while (code.code.get(dp.start).definition instanceof JumpIns) {
          if (dp instanceof GraphPartMulti) {
          dp = ((GraphPartMulti) dp).parts.get(0);
          }
          dp = dp.nextParts.get(0);
          }
          if (code.code.get(dp.start).definition instanceof PushByteIns) {
          hasDefault = true;
          }
          List<TreeItem> caseValues = new ArrayList<TreeItem>();
          for (int i = 0; i < switchLoc.nextParts.size() - 1; i++) {
          if (caseValuesMap.containsKey(i)) {
          caseValues.add(caseValuesMap.get(i));
          } else {
          continue;
          }
          }

          List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
          GraphPart next = null;

          List<GraphPart> loopContinues = getLoopsContinues(loops);

          next = switchLoc.getNextPartPath(loopContinues);
          if (next == null) {
          next = switchLoc.getNextSuperPartPath(loopContinues);
          }
          TreeItem ti = checkLoop(next, stopPart, loops);
          Loop currentLoop = new Loop(null, next);
          loops.add(currentLoop);
          //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
          List<Integer> valuesMapping = new ArrayList<Integer>();
          List<GraphPart> caseBodies = new ArrayList<GraphPart>();
          for (int i = 0; i < caseValues.size(); i++) {
          GraphPart cur = switchLoc.nextParts.get(1 + i);
          if (!caseBodies.contains(cur)) {
          caseBodies.add(cur);
          }
          valuesMapping.add(caseBodies.indexOf(cur));
          }

          List<TreeItem> defaultCommands = new ArrayList<TreeItem>();
          GraphPart defaultPart = null;
          if (hasDefault) {
          defaultPart = switchLoc.nextParts.get(switchLoc.nextParts.size() - 1);
          defaultCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, switchLoc, defaultPart, next, loops, localRegs, body, ignoredSwitches);
          }

          List<GraphPart> ignored = new ArrayList<GraphPart>();
          for (Loop l : loops) {
          ignored.add(l.loopContinue);
          }

          for (int i = 0; i < caseBodies.size(); i++) {
          List<TreeItem> cc = new ArrayList<TreeItem>();
          GraphPart nextCase = null;
          nextCase = next;
          if (next != null) {
          if (i < caseBodies.size() - 1) {
          if (!caseBodies.get(i).leadsTo(caseBodies.get(i + 1), ignored)) {
          cc.add(new BreakTreeItem(null, next.start));
          } else {
          nextCase = caseBodies.get(i + 1);
          }
          } else if (hasDefault) {
          if (!caseBodies.get(i).leadsTo(defaultPart, ignored)) {
          cc.add(new BreakTreeItem(null, next.start));
          } else {
          nextCase = defaultPart;
          }
          }
          }
          cc.addAll(0, printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, switchLoc, caseBodies.get(i), nextCase, loops, localRegs, body, ignoredSwitches));
          caseCommands.add(cc);
          }

          SwitchTreeItem sti = new SwitchTreeItem(null, next == null ? -1 : next.start, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
          ret.add(sti);
          loops.remove(currentLoop);
          if (next != null) {
          if (ti != null) {
          ret.add(ti);
          } else {
          ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, null, next, stopPart, loops, localRegs, body, ignoredSwitches));
          }
          }
          return ret;
          }*/
         //((IfTypeIns)ins.definition).translateInverted(new HashMap<Integer,TreeItem>(), co.stack, ins);
      }

      /*int ip = part.start;
       int addr = code.fixAddrAfterDebugLine(code.pos2adr(part.start));
       int maxend = -1;
       List<ABCException> catchedExceptions = new ArrayList<ABCException>();
       for (int e = 0; e < body.exceptions.length; e++) {
       if (addr == code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
       if (!body.exceptions[e].isFinally()) {
       if (((body.exceptions[e].end) > maxend) && (!parsedExceptions.contains(body.exceptions[e]))) {
       catchedExceptions.clear();
       maxend = code.fixAddrAfterDebugLine(body.exceptions[e].end);
       catchedExceptions.add(body.exceptions[e]);
       } else if (code.fixAddrAfterDebugLine(body.exceptions[e].end) == maxend) {
       catchedExceptions.add(body.exceptions[e]);
       }
       }
       }
       }
       if (catchedExceptions.size() > 0) {
       parsedExceptions.addAll(catchedExceptions);
       int endpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(0).end));
       int endposStartBlock = code.adr2pos(catchedExceptions.get(0).end);


       List<List<TreeItem>> catchedCommands = new ArrayList<List<TreeItem>>();
       if (code.code.get(endpos).definition instanceof JumpIns) {
       int afterCatchAddr = code.pos2adr(endpos + 1) + code.code.get(endpos).operands[0];
       int afterCatchPos = code.adr2pos(afterCatchAddr);
       Collections.sort(catchedExceptions, new Comparator<ABCException>() {
       public int compare(ABCException o1, ABCException o2) {
       try {
       return code.fixAddrAfterDebugLine(o1.target) - code.fixAddrAfterDebugLine(o2.target);
       } catch (ConvertException ex) {
       return 0;
       }
       }
       });


       List<TreeItem> finallyCommands = new ArrayList<TreeItem>();
       int returnPos = afterCatchPos;
       for (int e = 0; e < body.exceptions.length; e++) {
       if (body.exceptions[e].isFinally()) {
       if (addr == code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
       if (afterCatchPos + 1 == code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end))) {
       AVM2Instruction jmpIns = code.code.get(code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end)));
       if (jmpIns.definition instanceof JumpIns) {
       int finStart = code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytes().length + jmpIns.operands[0]);
       finallyJumps.add(finStart);
       for (int f = finStart; f < code.code.size(); f++) {
       if (code.code.get(f).definition instanceof LookupSwitchIns) {
       AVM2Instruction swins = code.code.get(f);
       if (swins.operands.length >= 3) {
       if (swins.operands[0] == swins.getBytes().length) {
       if (code.adr2pos(code.pos2adr(f) + swins.operands[2]) < finStart) {
       GraphPart fpart = null;
       for (GraphPart p : allParts) {
       if (p.start == finStart) {
       fpart = p;
       break;
       }
       }
       stack.push(new ExceptionTreeItem(body.exceptions[e]));
       GraphPart fepart = null;
       for (GraphPart p : allParts) {
       if (p.start == f + 1) {
       fepart = p;
       break;
       }
       }
       //code.code.get(f).ignored = true;
       ignoredSwitches.add(f);
       finallyCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, fpart, fepart, loops, localRegs, body, ignoredSwitches);
       returnPos = f + 1;
       break;
       }
       }
       }
       }
       }

       break;
       }
       }
       }
       }
       }

       for (int e = 0; e < catchedExceptions.size(); e++) {
       int eendpos;
       if (e < catchedExceptions.size() - 1) {
       eendpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(e + 1).target)) - 2;
       } else {
       eendpos = afterCatchPos - 1;
       }
       Stack<TreeItem> substack = new Stack<TreeItem>();
       substack.add(new ExceptionTreeItem(catchedExceptions.get(e)));

       GraphPart npart = null;
       int findpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(e).target));
       for (GraphPart p : allParts) {
       if (p.start == findpos) {
       npart = p;
       break;
       }
       }

       GraphPart nepart = null;
       for (GraphPart p : allParts) {
       if (p.start == eendpos + 1) {
       nepart = p;
       break;
       }
       }
       stack.add(new ExceptionTreeItem(catchedExceptions.get(e)));
       catchedCommands.add(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, npart, nepart, loops, localRegs, body, ignoredSwitches));
       }

       GraphPart nepart = null;

       for (GraphPart p : allParts) {
       if (p.start == endposStartBlock) {
       nepart = p;
       break;
       }
       }
       List<TreeItem> tryCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, part, nepart, loops, localRegs, body, ignoredSwitches);

       output.clear();
       output.add(new TryTreeItem(tryCommands, catchedExceptions, catchedCommands, finallyCommands));
       ip = returnPos;
       addr = code.pos2adr(ip);
       }

       }

       if (ip != part.start) {
       part = null;
       for (GraphPart p : allParts) {
       List<GraphPart> ps = p.getSubParts();
       for (GraphPart p2 : ps) {
       if (p2.start == ip) {
       part = p2;
       break;
       }
       }
       }
       ret.addAll(output);
       TreeItem lop = checkLoop(part, stopPart, loops);
       if (lop == null) {
       ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, null, part, stopPart, loops, localRegs, body, ignoredSwitches));
       } else {
       ret.add(lop);
       }
       return ret;
       }*/

      List<GraphTargetItem> retChecked = null;
      if ((retChecked = check(localData, allParts, stack, parent, part, stopPart, loops, output, forFinalCommands)) != null) {
         ret.addAll(retChecked);
         return ret;
      }
      List<GraphPart> loopContinues = getLoopsContinues(loops);
      boolean loop = false;
      boolean reversed = false;
      boolean whileTrue = false;
      Loop whileTrueLoop = null;
      if ((!part.nextParts.isEmpty()) && part.nextParts.get(0).leadsTo(part, loopContinues)) {
         if ((part.nextParts.size() > 1) && part.nextParts.get(1).leadsTo(part, loopContinues)) {
            if (output.isEmpty()) {
               whileTrueLoop = new Loop(loops.size(),part, null);
               loops.add(whileTrueLoop);
               whileTrue = true;
            } else {
               loop = true;//doWhile
            }

         } else {
            loop = true;
         }
      } else if ((part.nextParts.size() > 1) && part.nextParts.get(1).leadsTo(part, loopContinues)) {
         loop = true;
         reversed = true;
      }
      if (((part.nextParts.size() == 2) || ((part.nextParts.size() == 1) && loop)) /*&& (!isSwitch)*/) {

         boolean doWhile = loop;
         if (loop && output.isEmpty()) {
            doWhile = false;
         }
         Loop currentLoop = null;
         if (loop) {
            currentLoop=new Loop(loops.size(),part, null);
            loops.add(currentLoop);
         }

         loopContinues = new ArrayList<GraphPart>();
         for (Loop l : loops) {
            if (l.loopContinue != null) {
               loopContinues.add(l.loopContinue);
            }
         }

         if (loop && (part.nextParts.size() > 1) && (!doWhile)) {
            currentLoop.loopBreak = part.nextParts.get(reversed ? 0 : 1);
         }

         forFinalCommands.put(currentLoop, new ArrayList<GraphTargetItem>());

         GraphTargetItem expr = null;
         if (part.nextParts.size() == 1) {
            expr = new TrueItem(null);
         } else {
            if (!stack.isEmpty()) {
               expr = stack.pop();
            }
         }
         if (loop) {
            GraphTargetItem expr2 = expr;
            if (expr2 instanceof NotItem) {
               expr2 = ((NotItem) expr2).getOriginal();
            }
            if (expr2 instanceof AndItem) {
               currentLoop.loopContinue = ((AndItem) expr2).firstPart;
            }
            if (expr2 instanceof OrItem) {
               currentLoop.loopContinue = ((OrItem) expr2).firstPart;
            }
         }

         if (doWhile) {
            //ret.add(new DoWhileTreeItem(null, currentLoop.id, part.start, output, expr));
         } else {
            ret.addAll(output);
         }
         GraphPart loopBodyStart = null;
         GraphPart next = part.getNextPartPath(loopContinues);
         if (((!reversed) || loop) && (expr instanceof LogicalOpItem)) {
            expr = ((LogicalOpItem) expr).invert();
         }
         List<GraphTargetItem> retx = ret;
         if ((!loop) || (doWhile && (part.nextParts.size() > 1))) {
            if (doWhile) {
               retx = output;

            }
            int stackSizeBefore = stack.size();
            Stack<GraphTargetItem> trueStack = (Stack<GraphTargetItem>) stack.clone();
            Stack<GraphTargetItem> falseStack = (Stack<GraphTargetItem>) stack.clone();
            GraphTargetItem lopTrue = checkLoop(part.nextParts.get(1), stopPart, loops);
            GraphTargetItem lopFalse = null;
            if (next != part.nextParts.get(0)) {
               lopFalse = checkLoop(part.nextParts.get(0), stopPart, loops);
            }
            List<GraphTargetItem> onTrue = new ArrayList<GraphTargetItem>();
            if (lopTrue != null) {
               onTrue.add(lopTrue);
            } else {
               if (debugMode) {
                  System.err.println("ONTRUE: (inside " + part + ")");
               }
               onTrue = printGraph(localData, trueStack, allParts, part, part.nextParts.get(1), next == null ? stopPart : next, loops, forFinalCommands);
               if (debugMode) {
                  System.err.println("/ONTRUE (inside " + part + ")");
               }
            }
            List<GraphTargetItem> onFalse = new ArrayList<GraphTargetItem>();
            if (lopFalse != null) {
               onFalse.add(lopFalse);
            } else {
               if (debugMode) {
                  System.err.println("ONFALSE: (inside " + part + ")");
               }
               onFalse = (((next == part.nextParts.get(0)) || (part.nextParts.get(0).path.equals(part.path) || part.nextParts.get(0).path.length() < part.path.length())) ? new ArrayList<GraphTargetItem>() : printGraph(localData, falseStack, allParts, part, part.nextParts.get(0), next == null ? stopPart : next, loops, forFinalCommands));
               if (debugMode) {
                  System.err.println("/ONFALSE (inside " + part + ")");
               }
            }

            if (onTrue.isEmpty() && onFalse.isEmpty() && (trueStack.size() > stackSizeBefore) && (falseStack.size() > stackSizeBefore)) {
               stack.push(new TernarOpItem(null, expr, trueStack.pop(), falseStack.pop()));
            } else {
               List<GraphTargetItem> retw = retx;
               if (whileTrue) {
                  retw = new ArrayList<GraphTargetItem>();
                  retw.add(new IfItem(null, expr, onTrue, onFalse));
                  retx.add(new WhileItem(null, whileTrueLoop, new TrueItem(null), retw));
               } else {
                  retx.add(new IfItem(null, expr, onTrue, onFalse));
               }

               //Same continues in onTrue and onFalse gets continue on parent level
               if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                  if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                     if (onFalse.get(onFalse.size() - 1) instanceof ContinueItem) {
                        if (((ContinueItem) onTrue.get(onTrue.size() - 1)).loopId == ((ContinueItem) onFalse.get(onFalse.size() - 1)).loopId) {
                           onTrue.remove(onTrue.size() - 1);
                           retw.add(onFalse.remove(onFalse.size() - 1));
                        }
                     }
                  }
               }

               if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                  if (onTrue.get(onTrue.size() - 1) instanceof ExitItem) {
                     if (onFalse.get(onFalse.size() - 1) instanceof ContinueItem) {
                        retw.add(onFalse.remove(onFalse.size() - 1));
                     }
                  }
               }

               if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                  if (onFalse.get(onFalse.size() - 1) instanceof ExitItem) {
                     if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                        retw.add(onTrue.remove(onTrue.size() - 1));
                     }
                  }
               }
               if (whileTrue) {
                  checkContinueAtTheEnd(retw, whileTrueLoop);
               }
            }
            if (doWhile) {
               loopBodyStart = next;
            }
         }
         if (loop) { // && (!doWhile)) {
            List<GraphTargetItem> loopBody = new ArrayList<GraphTargetItem>();
            List<GraphTargetItem> finalCommands = null;
            GraphPart finalPart = null;
            GraphTargetItem ti;
            if ((loopBodyStart != null) && ((ti = checkLoop(loopBodyStart, stopPart, loops)) != null)) {
               loopBody.add(ti);
            } else {
               if (!(doWhile && (loopBodyStart == null))) {
                  loopBody = printGraph(localData, stack, allParts, part, loopBodyStart != null ? loopBodyStart : part.nextParts.get(reversed ? 1 : 0), stopPart, loops, forFinalCommands);

               }
            }
            checkContinueAtTheEnd(loopBody, currentLoop);
            finalCommands = forFinalCommands.get(currentLoop);
            if (!finalCommands.isEmpty()) {
               ret.add(new ForTreeItem(null, currentLoop, new ArrayList<GraphTargetItem>(), expr, finalCommands, loopBody));
            } /*else if ((expr instanceof HasNextTreeItem) && ((HasNextTreeItem) expr).collection.getNotCoerced().getThroughRegister() instanceof FilteredCheckTreeItem) {
             TreeItem gti = ((HasNextTreeItem) expr).collection.getNotCoerced().getThroughRegister();
             boolean found = false;
             if ((loopBody.size() == 3) || (loopBody.size() == 4)) {
             TreeItem ft = loopBody.get(0);
             if (ft instanceof WithTreeItem) {
             ft = loopBody.get(1);
             if (ft instanceof IfItem) {
             IfItem ift = (IfItem) ft;
             if (ift.onTrue.size() > 0) {
             ft = ift.onTrue.get(0);
             if (ft instanceof SetPropertyTreeItem) {
             SetPropertyTreeItem spt = (SetPropertyTreeItem) ft;
             if (spt.object instanceof LocalRegTreeItem) {
             int regIndex = ((LocalRegTreeItem) spt.object).regIndex;
             HasNextTreeItem iti = (HasNextTreeItem) expr;
             localRegs.put(regIndex, new FilterTreeItem(null, iti.collection.getThroughRegister(), ift.expression));
             }
             }
             }
             }
             }
             }
             } else if ((expr instanceof HasNextTreeItem) && (!loopBody.isEmpty()) && (loopBody.get(0) instanceof SetTypeTreeItem) && (((SetTypeTreeItem) loopBody.get(0)).getValue().getNotCoerced() instanceof NextValueTreeItem)) {
             TreeItem obj = ((SetTypeTreeItem) loopBody.get(0)).getObject();
             loopBody.remove(0);
             ret.add(new ForEachInTreeItem(null, currentLoop.id, part.start, new InTreeItem(expr.instruction, obj, ((HasNextTreeItem) expr).collection), loopBody));
             } else if ((expr instanceof HasNextTreeItem) && (!loopBody.isEmpty()) && (loopBody.get(0) instanceof SetTypeTreeItem) && (((SetTypeTreeItem) loopBody.get(0)).getValue().getNotCoerced() instanceof NextNameTreeItem)) {
             TreeItem obj = ((SetTypeTreeItem) loopBody.get(0)).getObject();
             loopBody.remove(0);
             ret.add(new ForInTreeItem(null, currentLoop.id, part.start, new InTreeItem(expr.instruction, obj, ((HasNextTreeItem) expr).collection), loopBody));
             }*/ else {
               if (doWhile) {
                  if (stack.isEmpty()) {
                     expr = new TrueItem(null);
                  } else {
                     expr = stack.pop();
                  }
                  loopBody.addAll(0, output);
                  checkContinueAtTheEnd(loopBody, currentLoop);

                  List<GraphTargetItem> addIf = new ArrayList<GraphTargetItem>();
                  if ((!loopBody.isEmpty()) && (loopBody.get(loopBody.size() - 1) instanceof IfItem)) {
                     IfItem ift = (IfItem) loopBody.get(loopBody.size() - 1);
                     if (ift.onFalse.isEmpty() || ((ift.onFalse.size() == 1) && (ift.onFalse.get(0) instanceof ContinueItem) && (((ContinueItem) ift.onFalse.get(0)).loopId == currentLoop.id))) {
                        expr = ift.expression;
                        addIf = ift.onTrue;
                        loopBody.remove(loopBody.size() - 1);
                     }
                  }
                  ret.add(new DoWhileItem(null, currentLoop, loopBody, expr));
                  ret.addAll(addIf);

               } else {
                  ret.add(new WhileItem(null, currentLoop, expr, loopBody));
               }
            }
         }
         if ((!doWhile) && (!whileTrue) && loop && (part.nextParts.size() > 1)) {
            loops.remove(currentLoop); //remove loop so no break shows up
            //ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level, part, part.nextParts.get(reversed ? 0 : 1), stopPart, loops, localRegs, body, ignoredSwitches));
            next = part.nextParts.get(reversed ? 0 : 1);
         }
         if (doWhile) {
            next = null;
         }
         if (next != null) {
            GraphTargetItem ti = checkLoop(next, stopPart, loops);
            if (ti != null) {
               ret.add(ti);
            } else {
               if (debugMode) {
                  System.err.println("NEXT: (inside " + part + ")");
               }
               ret.addAll(printGraph(localData, stack, allParts, part, next, stopPart, loops, forFinalCommands));
               if (debugMode) {
                  System.err.println("/NEXT: (inside " + part + ")");
               }
            }

         }
      } else {
         ret.addAll(output);
      }
      onepart:
      if (part.nextParts.size() == 1 && (!loop)) {
         /*if (part.end - part.start > 4) {
          if (code.code.get(part.end).definition instanceof PopIns) {
          if (code.code.get(part.end - 1).definition instanceof LabelIns) {
          if (code.code.get(part.end - 2).definition instanceof PushByteIns) {

          //if (code.code.get(part.end - 3).definition instanceof SetLocalTypeIns) {
          if (part.nextParts.size() == 1) {
          GraphPart sec = part.nextParts.get(0);

          if (code.code.get(sec.end).definition instanceof ReturnValueIns) {
          if (sec.end - sec.start >= 3) {
          if (code.code.get(sec.end - 1).definition instanceof KillIns) {
          if (code.code.get(sec.end - 2).definition instanceof GetLocalTypeIns) {
          if (!output.isEmpty()) {
          if (output.get(output.size() - 1) instanceof SetLocalTreeItem) {
          sec.ignored = true;
          ret.add(new ReturnValueTreeItem(code.code.get(sec.end), ((SetLocalTreeItem) output.get(output.size() - 1)).value));
          break onepart;
          }
          }
          }
          }
          }

          } else if (code.code.get(sec.end).definition instanceof ReturnVoidIns) {
          ret.add(new ReturnVoidTreeItem(code.code.get(sec.end)));
          break onepart;
          }
          //}
          }
          }
          }
          }
          }

          for (int f : finallyJumps) {
          if (part.nextParts.get(0).start == f) {
          if ((!output.isEmpty()) && (output.get(output.size() - 1) instanceof SetLocalTreeItem)) {
          ret.add(new ReturnValueTreeItem(null, ((SetLocalTreeItem) output.get(output.size() - 1)).value));
          } else {
          ret.add(new ReturnVoidTreeItem(null));
          }

          break onepart;
          }
          }
          */
         GraphPart p = part.nextParts.get(0);
         GraphTargetItem lop = checkLoop(p, stopPart, loops);
         if (lop == null) {
            if (p.path.length() == part.path.length()) {
               ret.addAll(printGraph(localData, stack, allParts, part, p, stopPart, loops, forFinalCommands));
            } else {
               if ((p != stopPart) && (p.refs.size() > 1)) {
                  List<GraphPart> nextList = new ArrayList<GraphPart>();
                  populateParts(p, nextList);
                  Loop nearestLoop = null;
                  loopn:
                  for (GraphPart n : nextList) {
                     for (Loop l : loops) {
                        if (l.loopContinue == n) {
                           nearestLoop = l;
                           break loopn;
                        }
                     }
                  }
                  if ((nearestLoop != null) && (nearestLoop.loopBreak != null)) {
                     List<GraphTargetItem> finalCommands = printGraph(localData, stack, allParts, part, p, nearestLoop.loopContinue, loops, forFinalCommands);
                     nearestLoop.loopContinue = p;
                     forFinalCommands.put(nearestLoop, finalCommands);
                     ContinueItem cti = new ContinueItem(null, nearestLoop.id);
                     ret.add(cti);
                  }
               }
            }
         } else {
            ret.add(lop);
         }
         //}
         //ret += (strOfChars(level, TAB) + "continue;\r\n");
         //}
      }
      /*if (isSwitch && (!ignoredSwitches.contains(part.end))) {
       //ret.add(new CommentTreeItem(code.code.get(part.end), "Switch not supported"));
       TreeItem switchedObject = stack.pop();
       List<TreeItem> caseValues = new ArrayList<TreeItem>();
       List<Integer> valueMappings = new ArrayList<Integer>();
       List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();

       GraphPart next = part.getNextPartPath(loopContinues);
       int breakPos = -1;
       if (next != null) {
       breakPos = next.start;
       }
       List<TreeItem> defaultCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(0), stopPart, loops, localRegs, body, ignoredSwitches);

       for (int i = 0; i < part.nextParts.size() - 1; i++) {
       caseValues.add(new IntegerValueTreeItem(null, (Long) (long) i));
       valueMappings.add(i);
       GraphPart nextCase = next;
       List<TreeItem> caseBody = new ArrayList<TreeItem>();
       if (i < part.nextParts.size() - 1 - 1) {
       if (!part.nextParts.get(1 + i).leadsTo(part.nextParts.get(1 + i + 1), new ArrayList<GraphPart>())) {
       caseBody.add(new BreakTreeItem(null, breakPos));
       } else {
       nextCase = part.nextParts.get(1 + i + 1);
       }
       } else if (!part.nextParts.get(1 + i).leadsTo(part.nextParts.get(0), new ArrayList<GraphPart>())) {
       caseBody.add(new BreakTreeItem(null, breakPos));
       } else {
       nextCase = part.nextParts.get(0);
       }
       caseBody.addAll(0, printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(1 + i), nextCase, loops, localRegs, body, ignoredSwitches));
       caseCommands.add(caseBody);
       }

       SwitchTreeItem swt = new SwitchTreeItem(null, breakPos, switchedObject, caseValues, caseCommands, defaultCommands, valueMappings);
       ret.add(swt);

       if (next != null) {
       TreeItem lopNext = checkLoop(next, stopPart, loops);
       if (lopNext != null) {
       ret.add(lopNext);
       } else {
       ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, next, stopPart, loops, localRegs, body, ignoredSwitches));
       }
       }

       }*/

      /*code.clearTemporaryRegisters(ret);
       if (!part.forContinues.isEmpty()) {
       throw new ForException(new ArrayList<TreeItem>(), ret, part);
       }*/
      return ret;
   }

   private List<GraphPart> makeGraph(GraphSource code, List<GraphPart> allBlocks, List<Integer> alternateEntries) {
      HashMap<Integer, List<Integer>> refs = code.visitCode(alternateEntries);
      List<GraphPart> ret = new ArrayList<GraphPart>();
      boolean visited[] = new boolean[code.size()];
      ret.add(makeGraph(null, "0", code, 0, 0, allBlocks, refs, visited));
      for (int pos : alternateEntries) {
         GraphPart e1 = new GraphPart(-1, -1);
         e1.path = "e";
         ret.add(makeGraph(e1, "e", code, pos, pos, allBlocks, refs, visited));
      }
      return ret;
   }

   private GraphPart makeGraph(GraphPart parent, String path, GraphSource code, int startip, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean visited2[]) {

      int ip = startip;
      for (GraphPart p : allBlocks) {
         if (p.start == ip) {
            p.refs.add(parent);
            return p;
         }
      }
      GraphPart g;
      GraphPart ret = new GraphPart(ip, -1);
      ret.path = path;
      GraphPart part = ret;
      while (ip < code.size()) {
         if (visited2[ip] || ((ip != startip) && (refs.get(ip).size() > 1))) {
            part.end = lastIp;
            GraphPart found = null;
            for (GraphPart p : allBlocks) {
               if (p.start == ip) {
                  found = p;
                  break;
               }
            }

            allBlocks.add(part);

            if (found != null) {
               part.nextParts.add(found);
               found.refs.add(part);
               break;
            } else {
               GraphPart gp = new GraphPart(ip, -1);
               gp.path = path;
               part.nextParts.add(gp);
               gp.refs.add(part);
               part = gp;
            }
         }
         lastIp = ip;
         GraphSourceItem ins = code.get(ip);
         if (ins.isExit()) {
            part.end = ip;
            allBlocks.add(part);
            break;
         } else if (ins.isJump()) {
            part.end = ip;
            allBlocks.add(part);
            ip = ins.getBranches(code).get(0);
            part.nextParts.add(g = makeGraph(part, path, code, ip, lastIp, allBlocks, refs, visited2));
            g.refs.add(part);
            break;
         } else if (ins.isBranch()) {
            part.end = ip;
            allBlocks.add(part);
            List<Integer> branches = ins.getBranches(code);
            for (int i = 0; i < branches.size(); i++) {
               part.nextParts.add(g = makeGraph(part, path + i, code, branches.get(i), ip, allBlocks, refs, visited2));
               g.refs.add(part);
            }
            break;
         }
         ip++;
      }
      if ((part.end == -1) && (ip >= code.size())) {
         if (part.start == code.size()) {
            part.end = code.size();
            allBlocks.add(part);
         } else {
            part.end = ip - 1;
            for (GraphPart p : allBlocks) {
               if (p.start == ip) {
                  p.refs.add(part);
                  part.nextParts.add(p);
                  return ret;
               }
            }
            GraphPart gp = new GraphPart(ip, ip);
            allBlocks.add(gp);
            gp.refs.add(part);
            part.nextParts.add(gp);
            allBlocks.add(part);
         }
      }
      return ret;
   }
   /**
    * String used to indent line when converting to string
    */
   public static final String INDENTOPEN = "INDENTOPEN";
   /**
    * String used to unindent line when converting to string
    */
   public static final String INDENTCLOSE = "INDENTCLOSE";
   private static final String INDENT_STRING = "   ";

   private static String tabString(int len) {
      String ret = "";
      for (int i = 0; i < len; i++) {
         ret += INDENT_STRING;
      }
      return ret;
   }

   /**
    * Converts list of TreeItems to string
    *
    * @param tree List of TreeItem
    * @return String
    */
   public static String graphToString(List<GraphTargetItem> tree, Object... localData) {
      StringBuilder ret = new StringBuilder();
      List localDataList = new ArrayList();
      for (Object o : localData) {
         localDataList.add(o);
      }
      for (GraphTargetItem ti : tree) {
         ret.append(ti.toStringSemicoloned(localDataList));
         ret.append("\r\n");
      }
      String parts[] = ret.toString().split("\r\n");
      ret = new StringBuilder();


      try {
         Stack<String> loopStack = new Stack<String>();
         for (int p = 0; p < parts.length; p++) {
            String stripped = Highlighting.stripHilights(parts[p]);
            if (stripped.endsWith(":") && (!stripped.startsWith("case ")) && (!stripped.equals("default:"))) {
               loopStack.add(stripped.substring(0, stripped.length() - 1));
            }
            if (stripped.startsWith("break ")) {
               if (stripped.equals("break " + loopStack.peek().replace("switch", "") + ";")) {
                  parts[p] = parts[p].replace(" " + loopStack.peek().replace("switch", ""), "");
               }
            }
            if (stripped.startsWith("continue ")) {
               if (loopStack.size() > 0) {
                  int pos = loopStack.size() - 1;
                  String loopname = "";
                  do {
                     loopname = loopStack.get(pos);
                     pos--;
                  } while ((pos >= 0) && (loopname.startsWith("loopswitch")));
                  if (stripped.equals("continue " + loopname + ";")) {
                     parts[p] = parts[p].replace(" " + loopname, "");
                  }
               }
            }
            if (stripped.startsWith(":")) {
               loopStack.pop();
            }
         }
      } catch (Exception ex) {
      }

      int level = 0;
      for (int p = 0; p < parts.length; p++) {
         String strippedP = Highlighting.stripHilights(parts[p]).trim();
         if (strippedP.endsWith(":") && (!strippedP.startsWith("case ")) && (!strippedP.equals("default:"))) {
            String loopname = strippedP.substring(0, strippedP.length() - 1);
            boolean dorefer = false;
            for (int q = p + 1; q < parts.length; q++) {
               String strippedQ = Highlighting.stripHilights(parts[q]).trim();
               if (strippedQ.equals("break " + loopname + ";")) {
                  dorefer = true;
                  break;
               }
               if (strippedQ.equals("continue " + loopname + ";")) {
                  dorefer = true;
                  break;
               }
               if (strippedQ.equals(":" + loopname)) {
                  break;
               }
            }
            if (!dorefer) {
               continue;
            }
         }
         if (strippedP.startsWith(":")) {
            continue;
         }
         if (Highlighting.stripHilights(parts[p]).equals(INDENTOPEN)) {
            level++;
            continue;
         }
         if (Highlighting.stripHilights(parts[p]).equals(INDENTCLOSE)) {
            level--;
            continue;
         }
         if (Highlighting.stripHilights(parts[p]).equals("}")) {
            level--;
         }
         if (Highlighting.stripHilights(parts[p]).equals("};")) {
            level--;
         }
         ret.append(tabString(level));
         ret.append(parts[p]);
         ret.append("\r\n");
         if (Highlighting.stripHilights(parts[p]).equals("{")) {
            level++;
         }
      }
      return ret.toString();
   }
}